package com.lotoproba.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lotoproba.app.data.api.ApiKeyManager
import com.lotoproba.app.data.api.GeminiLotoService
import com.lotoproba.app.data.db.LotoDatabase
import com.lotoproba.app.data.model.AiEvaluationRecord
import com.lotoproba.app.data.model.LotoDraw
import com.lotoproba.app.data.model.LotoSource
import com.lotoproba.app.data.model.ProposedCombination
import com.lotoproba.app.data.network.DataImporterExporter
import com.lotoproba.app.data.network.ImportReport
import com.lotoproba.app.data.repository.LotoRepository
import com.lotoproba.app.stats.ChiSquareResult
import com.lotoproba.app.stats.DailyNapPackage
import com.lotoproba.app.stats.GapAndColdAnalysis
import com.lotoproba.app.stats.GeneratedCombination
import com.lotoproba.app.stats.LotoStatsCalculator
import com.lotoproba.app.stats.NapItem
import com.lotoproba.app.stats.NumberFrequency
import com.lotoproba.app.stats.NumberGapStat
import com.lotoproba.app.stats.NumberStat
import com.lotoproba.app.stats.PairStat
import com.lotoproba.app.stats.PairsAndTriosAnalysis
import com.lotoproba.app.stats.TrackingSummary
import com.lotoproba.app.stats.TrioStat
import com.lotoproba.app.stats.UserNumbersAnalysis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class LotoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LotoRepository = LotoRepository(
        lotoDao = LotoDatabase.getDatabase(application).lotoDao(),
        appContext = application
    )
    val apiKeyManager = ApiKeyManager(application)
    private val aiService = GeminiLotoService(apiKeyManager)

    // Sources
    val sources: StateFlow<List<LotoSource>> = repository.allSources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSourceId = MutableStateFlow("niger_loto")
    val selectedSourceId: StateFlow<String> = _selectedSourceId.asStateFlow()

    // Tirages de la source sélectionnée
    val draws: StateFlow<List<LotoDraw>> = _selectedSourceId.flatMapLatest { sourceId ->
        repository.getDrawsFlow(sourceId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sélections archivées et suivi rétrospectif
    val allTrackedCombinations: StateFlow<List<ProposedCombination>> =
        _selectedSourceId.flatMapLatest { sourceId ->
            repository.getAllCombinationsFlow(sourceId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trackingSummary: StateFlow<TrackingSummary> = allTrackedCombinations.map { list ->
        val evaluated = list.filter { it.evaluated }
        LotoStatsCalculator.evaluateTrackingBalance(evaluated)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TrackingSummary(
            totalTestedCombinations = 0,
            totalMatches = 0,
            averageMatchesObserved = 0.0,
            averageMatchesExpected = 0.2747,
            count0 = 0,
            count1 = 0,
            count2 = 0,
            count3Plus = 0,
            conclusionText = "Aucun suivi enregistré"
        )
    )

    // Pondérations personnalisées pour les 5 combinaisons descriptives
    val weightRecent = MutableStateFlow(40f)
    val weightGlobal = MutableStateFlow(30f)
    val weightGap = MutableStateFlow(30f)

    // Numéro actuellement inspecté pour la courbe de l'historique
    val selectedNumberForCurve = MutableStateFlow(7)

    // États UI
    val isRefreshing = MutableStateFlow(false)
    val snackbarMessage = MutableStateFlow<String?>(null)
    val importReport = MutableStateFlow<ImportReport?>(null)
    val showFirstLaunchDialog = MutableStateFlow(false)
    val showManualImportDialog = MutableStateFlow(false)
    val showAddSourceDialog = MutableStateFlow(false)
    val showAddDrawDialog = MutableStateFlow(false)
    val editingSource = MutableStateFlow<LotoSource?>(null)

    // États de l'Intelligence Artificielle (IA)
    private val _customApiKey = MutableStateFlow(apiKeyManager.getCustomApiKey())
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()
    val isApiKeyConfigured = MutableStateFlow(apiKeyManager.isKeyConfigured())
    val isAiGenerating = MutableStateFlow(false)
    val isAiEvaluating = MutableStateFlow(false)

    val aiEvaluations: StateFlow<List<AiEvaluationRecord>> =
        _selectedSourceId.flatMapLatest { sourceId ->
            repository.getAiEvaluationsFlow(sourceId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestAiPrediction: StateFlow<AiEvaluationRecord?> = aiEvaluations.map { list ->
        list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Statistiques calculées réactivement
    val numberStats: StateFlow<List<NumberStat>> = draws.combine(_selectedSourceId) { list, _ ->
        LotoStatsCalculator.calculateNumberStats(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _numberFrequencies01To90 = MutableStateFlow(
        LotoStatsCalculator.calculateNumberFrequencies01To90(emptyList())
    )
    val numberFrequencies01To90: StateFlow<List<NumberFrequency>> =
        _numberFrequencies01To90.asStateFlow()

    val frequencyMap01To90: StateFlow<Map<String, Int>> = _numberFrequencies01To90.map { list ->
        list.associate { it.formattedNumber to it.count }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        (1..90).associate { it.toString().padStart(2, '0') to 0 }
    )

    private val _pairTrioLimit = MutableStateFlow(10)
    val pairTrioLimit: StateFlow<Int> = _pairTrioLimit.asStateFlow()

    private val _pairsAndTriosAnalysis = MutableStateFlow(
        PairsAndTriosAnalysis(
            totalDrawsAnalyzed = 0,
            topPairs = emptyList(),
            topTrios = emptyList()
        )
    )
    val pairsAndTriosAnalysis: StateFlow<PairsAndTriosAnalysis> =
        _pairsAndTriosAnalysis.asStateFlow()

    private val _recentWindowSize = MutableStateFlow(30)
    val recentWindowSize: StateFlow<Int> = _recentWindowSize.asStateFlow()

    private val _gapTopLimit = MutableStateFlow(10)

    private val _gapAndColdAnalysis = MutableStateFlow(
        LotoStatsCalculator.calculateNumberGapsAndLeastRecent(emptyList())
    )
    val gapAndColdAnalysis: StateFlow<GapAndColdAnalysis> =
        _gapAndColdAnalysis.asStateFlow()

    val numberGaps: StateFlow<List<NumberGapStat>> = gapAndColdAnalysis.map { it.numberGaps }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gapByNumberMap: StateFlow<Map<Int, Int>> = gapAndColdAnalysis.map { analysis ->
        analysis.numberGaps.associate { it.number to it.drawsSinceLastAppearance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val highestGapNumbers: StateFlow<List<NumberGapStat>> =
        gapAndColdAnalysis.map { it.longestGaps }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leastRecentlyDrawnNumbers: StateFlow<List<NumberGapStat>> =
        gapAndColdAnalysis.map { it.leastRecentlyDrawn }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userProposedNumbers = MutableStateFlow<List<Int>>(emptyList())
    val userProposedNumbers: StateFlow<List<Int>> = _userProposedNumbers.asStateFlow()

    private val _userNumbersAnalysis = MutableStateFlow<UserNumbersAnalysis?>(null)
    val userNumbersAnalysis: StateFlow<UserNumbersAnalysis?> = _userNumbersAnalysis.asStateFlow()

    private val _userNumbersAiReport = MutableStateFlow<String?>(null)
    val userNumbersAiReport: StateFlow<String?> = _userNumbersAiReport.asStateFlow()

    val isAnalyzingUserNumbersWithAi = MutableStateFlow(false)

    private val _chiSquareResult = MutableStateFlow(
        ChiSquareResult(0.0, 90, 113.145, 1.0, true, "Calcul des statistiques en cours...")
    )
    val chiSquareResult: StateFlow<ChiSquareResult> = _chiSquareResult.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeIfEmpty(application)
        }
        viewModelScope.launch {
            draws.collect { list ->
                _numberFrequencies01To90.value =
                    LotoStatsCalculator.calculateNumberFrequencies01To90(list)
                val limit = _pairTrioLimit.value
                _pairsAndTriosAnalysis.value =
                    LotoStatsCalculator.analyzeTopPairsAndTrios(
                        draws = list,
                        pairLimit = limit,
                        trioLimit = limit
                    )
                _gapAndColdAnalysis.value =
                    LotoStatsCalculator.calculateNumberGapsAndLeastRecent(
                        draws = list,
                        recentWindowSize = _recentWindowSize.value,
                        topLimit = _gapTopLimit.value
                    )
                _chiSquareResult.value = LotoStatsCalculator.calculateChiSquareTest(list)
                val userNums = _userProposedNumbers.value
                if (userNums.isNotEmpty()) {
                    _userNumbersAnalysis.value =
                        LotoStatsCalculator.analyzeUserProposedNumbers(userNums, list)
                }
            }
        }
    }

    val topPairs: StateFlow<List<PairStat>> = pairsAndTriosAnalysis.map { it.topPairs }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topTrios: StateFlow<List<TrioStat>> = pairsAndTriosAnalysis.map { it.topTrios }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combinaisons descriptives générées dynamiquement selon les pondérations
    val generatedCombinations: StateFlow<List<GeneratedCombination>> = combine(
        numberStats,
        weightRecent,
        weightGlobal,
        weightGap
    ) { stats, wRecent, wGlobal, wGap ->
        LotoStatsCalculator.generate5ProposedCombinations(stats, wRecent, wGlobal, wGap)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Synthèse NAP (NAP 1, NAP 2, NAP 3 et PERM 5) basée sur l'historique observé
    val dailyNapPackage: StateFlow<DailyNapPackage> = draws.combine(_selectedSourceId) { list, _ ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val targetDate = sdf.format(cal.time)
        LotoStatsCalculator.calculateDailyNapPackage(list, targetDate)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DailyNapPackage(
            targetDate = "Demain",
            mainBankerNap1 = NapItem("NAP 1 (Repère)", "N1", listOf(12), 50, "Calcul en cours...", ""),
            alternativeNap1 = listOf(45, 78),
            topNap2Pairs = emptyList(),
            topNap3Trios = emptyList(),
            perm5 = NapItem("PERM 5", "PERM5", listOf(12, 45, 78, 23, 56), 50, "", "")
        )
    )

    fun saveTodayNapPropositions() {
        val napPkg = dailyNapPackage.value
        val targetDate = napPkg.targetDate

        val entities = mutableListOf<ProposedCombination>()

        // 1. NAP 1
        entities.add(
            ProposedCombination(
                sourceId = _selectedSourceId.value,
                targetDateString = targetDate,
                rank = 1,
                numbersCsv = napPkg.mainBankerNap1.numbers.joinToString(","),
                scorePercent = napPkg.mainBankerNap1.confidencePercent.toDouble(),
                weightsSummary = "NAP 1 (Repère)",
                evaluated = false
            )
        )

        // 2. NAP 2 (Top 3 paires)
        napPkg.topNap2Pairs.forEachIndexed { idx, pair ->
            entities.add(
                ProposedCombination(
                    sourceId = _selectedSourceId.value,
                    targetDateString = targetDate,
                    rank = 2 + idx,
                    numbersCsv = pair.numbers.joinToString(","),
                    scorePercent = pair.confidencePercent.toDouble(),
                    weightsSummary = "NAP 2 n°${idx + 1}",
                    evaluated = false
                )
            )
        }

        // 3. NAP 3 (Top 2 trios)
        napPkg.topNap3Trios.forEachIndexed { idx, trio ->
            entities.add(
                ProposedCombination(
                    sourceId = _selectedSourceId.value,
                    targetDateString = targetDate,
                    rank = 5 + idx,
                    numbersCsv = trio.numbers.joinToString(","),
                    scorePercent = trio.confidencePercent.toDouble(),
                    weightsSummary = "NAP 3 n°${idx + 1}",
                    evaluated = false
                )
            )
        }

        // 4. PERM 5
        entities.add(
            ProposedCombination(
                sourceId = _selectedSourceId.value,
                targetDateString = targetDate,
                rank = 7,
                numbersCsv = napPkg.perm5.numbers.joinToString(","),
                scorePercent = napPkg.perm5.confidencePercent.toDouble(),
                weightsSummary = "PERM 5",
                evaluated = false
            )
        )

        viewModelScope.launch {
            repository.saveDailyPropositions(entities)
            snackbarMessage.value = "Synthèse NAP 1, NAP 2 et NAP 3 archivée pour suivi le $targetDate."
        }
    }

    fun selectSource(sourceId: String) {
        _selectedSourceId.value = sourceId
    }

    /**
     * Traite une liste de tirages stockés pour calculer la fréquence observée
     * de chaque numéro de 01 à 90.
     * Retourne une Map ordonnée de "01" à "90" -> nombre d'apparitions.
     */
    fun processStoredDrawsFrequency(storedDraws: List<LotoDraw>): Map<String, Int> {
        val frequencies = LotoStatsCalculator.calculateNumberFrequencies01To90(storedDraws)
        _numberFrequencies01To90.value = frequencies
        return frequencies.associate { it.formattedNumber to it.count }
    }

    /**
     * Charge les tirages stockés en base Room pour la source sélectionnée
     * et calcule la fréquence observée de chaque numéro de 01 à 90.
     */
    fun calculateNumberFrequencies01To90(sourceId: String = _selectedSourceId.value) {
        viewModelScope.launch {
            val storedDraws = repository.getStoredDraws(sourceId)
            val frequencies = LotoStatsCalculator.calculateNumberFrequencies01To90(storedDraws)
            _numberFrequencies01To90.value = frequencies
        }
    }

    /**
     * Analyse une liste donnée de tirages historiques pour identifier les paires
     * et trios de numéros les plus fréquemment tirés ensemble.
     */
    fun processHistoricalDrawsForPairsAndTrios(
        historicalDraws: List<LotoDraw>,
        limit: Int = _pairTrioLimit.value
    ): PairsAndTriosAnalysis {
        val safeLimit = limit.coerceIn(1, 50)
        _pairTrioLimit.value = safeLimit
        val analysis = LotoStatsCalculator.analyzeTopPairsAndTrios(
            draws = historicalDraws,
            pairLimit = safeLimit,
            trioLimit = safeLimit
        )
        _pairsAndTriosAnalysis.value = analysis
        return analysis
    }

    /**
     * Charge l'historique stocké dans Room pour la source sélectionnée et identifie
     * les paires et trios les plus fréquemment tirés ensemble.
     */
    fun analyzePairsAndTriosFromHistory(
        sourceId: String = _selectedSourceId.value,
        limit: Int = _pairTrioLimit.value
    ) {
        val safeLimit = limit.coerceIn(1, 50)
        _pairTrioLimit.value = safeLimit
        viewModelScope.launch {
            val storedDraws = repository.getStoredDraws(sourceId)
            _pairsAndTriosAnalysis.value = LotoStatsCalculator.analyzeTopPairsAndTrios(
                draws = storedDraws,
                pairLimit = safeLimit,
                trioLimit = safeLimit
            )
        }
    }

    /**
     * Met à jour le nombre de résultats affichés (Top N) et relance l'analyse des paires et trios.
     */
    fun updatePairTrioLimit(limit: Int) {
        val safeLimit = limit.coerceIn(1, 50)
        _pairTrioLimit.value = safeLimit
        val currentDraws = draws.value
        if (currentDraws.isNotEmpty()) {
            _pairsAndTriosAnalysis.value = LotoStatsCalculator.analyzeTopPairsAndTrios(
                draws = currentDraws,
                pairLimit = safeLimit,
                trioLimit = safeLimit
            )
        } else {
            analyzePairsAndTriosFromHistory(_selectedSourceId.value, safeLimit)
        }
    }

    /**
     * Calcule le nombre de tirages écoulés depuis la dernière apparition de chaque numéro (retards)
     * et identifie les numéros les moins sortis récemment à partir d'une liste de tirages.
     */
    fun processHistoricalDrawsForGapsAndCold(
        historicalDraws: List<LotoDraw>,
        recentWindowSize: Int = _recentWindowSize.value,
        topLimit: Int = _gapTopLimit.value
    ): GapAndColdAnalysis {
        val safeWindow = recentWindowSize.coerceIn(5, 180)
        val safeLimit = topLimit.coerceIn(1, LotoStatsCalculator.TOTAL_NUMBERS)
        _recentWindowSize.value = safeWindow
        _gapTopLimit.value = safeLimit
        val analysis = LotoStatsCalculator.calculateNumberGapsAndLeastRecent(
            draws = historicalDraws,
            recentWindowSize = safeWindow,
            topLimit = safeLimit
        )
        _gapAndColdAnalysis.value = analysis
        return analysis
    }

    /**
     * Charge les tirages stockés dans Room pour la source sélectionnée afin de calculer
     * les retards de chaque numéro et les numéros les moins sortis récemment.
     */
    fun calculateGapsAndLeastRecentFromHistory(
        sourceId: String = _selectedSourceId.value,
        recentWindowSize: Int = _recentWindowSize.value,
        topLimit: Int = _gapTopLimit.value
    ) {
        val safeWindow = recentWindowSize.coerceIn(5, 180)
        val safeLimit = topLimit.coerceIn(1, LotoStatsCalculator.TOTAL_NUMBERS)
        _recentWindowSize.value = safeWindow
        _gapTopLimit.value = safeLimit
        viewModelScope.launch {
            val storedDraws = repository.getStoredDraws(sourceId)
            _gapAndColdAnalysis.value = LotoStatsCalculator.calculateNumberGapsAndLeastRecent(
                draws = storedDraws,
                recentWindowSize = safeWindow,
                topLimit = safeLimit
            )
        }
    }

    /**
     * Met à jour la fenêtre d'analyse récente (nombre de derniers tirages) et recalcule les retards.
     */
    fun updateRecentWindowSize(windowSize: Int) {
        val safeWindow = windowSize.coerceIn(5, 180)
        _recentWindowSize.value = safeWindow
        val currentDraws = draws.value
        if (currentDraws.isNotEmpty()) {
            _gapAndColdAnalysis.value = LotoStatsCalculator.calculateNumberGapsAndLeastRecent(
                draws = currentDraws,
                recentWindowSize = safeWindow,
                topLimit = _gapTopLimit.value
            )
        } else {
            calculateGapsAndLeastRecentFromHistory(_selectedSourceId.value, safeWindow, _gapTopLimit.value)
        }
    }

    /**
     * Exécute le test d'équité du Chi-carré sur une liste de tirages pour comparer
     * la distribution observée des numéros (0 à 90) avec la distribution théorique uniforme.
     */
    fun runChiSquareEquityTest(historicalDraws: List<LotoDraw>): ChiSquareResult {
        val result = LotoStatsCalculator.calculateChiSquareTest(historicalDraws)
        _chiSquareResult.value = result
        return result
    }

    /**
     * Charge l'historique des tirages stockés dans Room pour la source active et exécute
     * le test d'équité du Chi-carré par rapport à une distribution uniforme.
     */
    fun runChiSquareEquityTestFromHistory(sourceId: String = _selectedSourceId.value) {
        viewModelScope.launch {
            val storedDraws = repository.getStoredDraws(sourceId)
            _chiSquareResult.value = LotoStatsCalculator.calculateChiSquareTest(storedDraws)
        }
    }

    /**
     * Compare une distribution observée fournie (numéro 0..90 -> nombre d'apparitions)
     * avec la distribution théorique uniforme via le test du Chi-carré.
     */
    fun runChiSquareEquityTestOnFrequencies(observedCountsByNumber: Map<Int, Int>): ChiSquareResult {
        val countsArray = IntArray(LotoStatsCalculator.TOTAL_NUMBERS) { number ->
            observedCountsByNumber[number]?.coerceAtLeast(0) ?: 0
        }
        val result = LotoStatsCalculator.calculateChiSquareFromObservedCounts(countsArray)
        _chiSquareResult.value = result
        return result
    }

    fun updateWeights(recent: Float, global: Float, gap: Float) {
        weightRecent.value = recent
        weightGlobal.value = global
        weightGap.value = gap
    }

    fun selectNumberForCurve(number: Int) {
        selectedNumberForCurve.value = number.coerceIn(0, 90)
    }

    /**
     * Actualise les tirages depuis la source sélectionnée (CSV embarqué ou page web avec timeout 15 s).
     */
    fun refreshCurrentSourceWeb() {
        val currentSource = sources.value.find { it.id == _selectedSourceId.value }
        if (currentSource == null) {
            snackbarMessage.value = "Source introuvable."
            return
        }

        viewModelScope.launch {
            isRefreshing.value = true
            val result = repository.refreshFromWebUrl(currentSource, getApplication())
            isRefreshing.value = false
            result.onSuccess { report ->
                importReport.value = report
                snackbarMessage.value =
                    "Mise à jour terminée : ${report.validCount} tirage(s) analysé(s) (${report.duplicateCount} doublon(s) ignoré(s))."
            }.onFailure { ex ->
                snackbarMessage.value =
                    "${ex.message ?: "Erreur lors du chargement."} Les données locales restent disponibles."
            }
        }
    }

    /**
     * Importation manuelle de texte ou CSV avec dédoublonnage automatique par date.
     */
    fun importManualText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            isRefreshing.value = true
            val report = repository.importManualContent(text, _selectedSourceId.value)
            isRefreshing.value = false
            importReport.value = report
            snackbarMessage.value =
                "Import terminé : ${report.validCount} tirage(s) valide(s), ${report.duplicateCount} doublon(s) ignoré(s), ${report.invalidCount} ligne(s) rejetée(s)."
        }
    }

    /**
     * Ajout d'un tirage manuel unitaire avec validation stricte et refus des doublons de date.
     */
    fun addSingleDraw(
        dateString: String,
        n1: Int,
        n2: Int,
        n3: Int,
        n4: Int,
        n5: Int,
        drawName: String
    ): Boolean {
        val draw = LotoDraw(
            sourceId = _selectedSourceId.value,
            dateString = dateString.trim(),
            drawName = drawName.ifBlank { "Tirage Quotidien" },
            n1 = n1,
            n2 = n2,
            n3 = n3,
            n4 = n4,
            n5 = n5
        )
        if (!draw.isValid()) {
            snackbarMessage.value = "Tirage invalide : 5 numéros distincts entre 0 et 90 requis."
            return false
        }

        viewModelScope.launch {
            val id = repository.insertDraw(draw)
            if (id > 0) {
                snackbarMessage.value = "Tirage du ${draw.dateString} enregistré avec succès."
            } else {
                snackbarMessage.value = "Un tirage existe déjà pour cette date (${draw.dateString})."
            }
        }
        return true
    }

    fun deleteDraw(draw: LotoDraw) {
        viewModelScope.launch {
            repository.deleteDraw(draw)
            snackbarMessage.value = "Tirage supprimé."
        }
    }

    /**
     * Archive les 5 combinaisons pondérées pour le suivi rétrospectif.
     */
    fun saveTodayPropositions() {
        val currentCombos = generatedCombinations.value
        if (currentCombos.isEmpty()) {
            snackbarMessage.value = "Aucune combinaison à enregistrer."
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val targetDate = sdf.format(cal.time)

        val wSummary =
            "Réc:${weightRecent.value.toInt()}% | Glob:${weightGlobal.value.toInt()}% | Ret:${weightGap.value.toInt()}%"

        val entities = currentCombos.map { combo ->
            ProposedCombination(
                sourceId = _selectedSourceId.value,
                targetDateString = targetDate,
                rank = combo.rank,
                numbersCsv = combo.numbers.joinToString(","),
                scorePercent = combo.scorePercent,
                weightsSummary = wSummary,
                evaluated = false
            )
        }

        viewModelScope.launch {
            repository.saveDailyPropositions(entities)
            snackbarMessage.value = "5 combinaisons archivées pour comparaison le $targetDate."
        }
    }

    /**
     * Ajout ou modification d'une source (pays / lien URL ou fichier local).
     */
    fun saveSource(name: String, country: String, url: String, parserPreset: String) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isBlank() || name.isBlank()) {
            snackbarMessage.value = "Veuillez renseigner le nom et l'URL."
            return
        }

        val existing = editingSource.value
        val sourceToSave = existing?.copy(
            name = name.trim(),
            country = country.trim(),
            url = trimmedUrl,
            parserPreset = parserPreset
        ) ?: LotoSource(
            id = "src_" + System.currentTimeMillis(),
            name = name.trim(),
            country = country.trim().ifBlank { "International" },
            url = trimmedUrl,
            parserPreset = parserPreset
        )

        viewModelScope.launch {
            repository.insertOrUpdateSource(sourceToSave)
            _selectedSourceId.value = sourceToSave.id
            editingSource.value = null
            showAddSourceDialog.value = false
            snackbarMessage.value = "Source '${sourceToSave.name}' enregistrée."
        }
    }

    fun deleteSource(source: LotoSource) {
        viewModelScope.launch {
            repository.deleteSource(source)
            val remaining = sources.value.filter { it.id != source.id }
            if (remaining.isNotEmpty()) {
                _selectedSourceId.value = remaining[0].id
            }
            snackbarMessage.value = "Source '${source.name}' supprimée."
        }
    }

    fun exportDrawsCsv(): String {
        val currentDraws = draws.value
        return DataImporterExporter.exportToCsv(currentDraws)
    }

    fun resetSampleData() {
        viewModelScope.launch {
            isRefreshing.value = true
            repository.resetWithSampleData(_selectedSourceId.value, getApplication())
            isRefreshing.value = false
            snackbarMessage.value = "Données locales rechargées depuis le CSV intégré (assets/niger_2years.csv)."
        }
    }

    fun clearSnackbar() {
        snackbarMessage.value = null
    }

    fun clearImportReport() {
        importReport.value = null
    }

    // --- Actions Intelligence Artificielle (IA) ---

    fun saveCustomApiKey(key: String) {
        apiKeyManager.setCustomApiKey(key)
        _customApiKey.value = apiKeyManager.getCustomApiKey()
        isApiKeyConfigured.value = apiKeyManager.isKeyConfigured()
        snackbarMessage.value = "Clé API enregistrée avec succès."
    }

    fun clearCustomApiKey() {
        apiKeyManager.clearCustomApiKey()
        _customApiKey.value = ""
        isApiKeyConfigured.value = apiKeyManager.isKeyConfigured()
        snackbarMessage.value = "Clé API personnalisée effacée."
    }

    fun generateAiPrediction() {
        viewModelScope.launch {
            isAiGenerating.value = true
            try {
                val currentSource = sources.value.find { it.id == _selectedSourceId.value }
                val sourceName = currentSource?.name ?: "Niger Loto (CSV intégré)"
                val result = aiService.generatePrediction(
                    recentDraws = draws.value,
                    numberStats = numberStats.value,
                    topPairs = topPairs.value,
                    sourceId = _selectedSourceId.value,
                    sourceName = sourceName
                )
                result.onSuccess { record ->
                    repository.saveAiEvaluation(record)
                    snackbarMessage.value = "Synthèse IA enregistrée pour le ${record.targetDate}."
                }.onFailure { err ->
                    snackbarMessage.value = "Erreur IA : ${err.localizedMessage}"
                }
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun evaluateLatestDrawWithAi() {
        val latestDraw = draws.value.firstOrNull()
        if (latestDraw == null) {
            snackbarMessage.value = "Aucun tirage disponible pour la comparaison."
            return
        }

        val targetSummary = aiEvaluations.value.firstOrNull { !it.evaluated } ?: aiEvaluations.value.firstOrNull()
        if (targetSummary == null) {
            snackbarMessage.value = "Aucune synthèse IA à comparer. Générez d'abord une synthèse."
            return
        }

        viewModelScope.launch {
            isAiEvaluating.value = true
            try {
                val result = aiService.evaluateDrawAgainstPrediction(
                    latestDraw = latestDraw,
                    prediction = targetSummary,
                    recentDraws = draws.value
                )
                result.onSuccess { evaluatedRecord ->
                    repository.saveAiEvaluation(evaluatedRecord)
                    snackbarMessage.value = "Comparaison rétrospective terminée."
                }.onFailure { err ->
                    snackbarMessage.value = "Erreur pendant la comparaison : ${err.localizedMessage}"
                }
            } finally {
                isAiEvaluating.value = false
            }
        }
    }

    fun deleteAiEvaluation(record: AiEvaluationRecord) {
        viewModelScope.launch {
            repository.deleteAiEvaluation(record)
            snackbarMessage.value = "Rapport supprimé."
        }
    }

    // --- Analyse des chiffres saisis par l'utilisateur ---

    /**
     * Analyse une sélection de 1 à 5 numéros fournie par l'utilisateur à partir
     * des tirages fournis (ou des tirages actuellement chargés).
     */
    fun analyzeUserProposedNumbers(
        numbers: List<Int>,
        historicalDraws: List<LotoDraw> = draws.value
    ): UserNumbersAnalysis? {
        val valid = numbers.filter { it in 0..90 }.distinct().sorted().take(5)
        if (valid.isEmpty()) {
            snackbarMessage.value = "Veuillez saisir au moins 1 numéro valide entre 0 et 90."
            return null
        }
        _userProposedNumbers.value = valid
        _userNumbersAiReport.value = null
        val analysis = LotoStatsCalculator.analyzeUserProposedNumbers(valid, historicalDraws)
        _userNumbersAnalysis.value = analysis
        return analysis
    }

    /**
     * Charge les tirages stockés dans Room pour la source active et analyse la sélection utilisateur.
     */
    fun analyzeUserProposedNumbersFromHistory(
        numbers: List<Int>,
        sourceId: String = _selectedSourceId.value
    ) {
        val valid = numbers.filter { it in 0..90 }.distinct().sorted().take(5)
        if (valid.isEmpty()) {
            snackbarMessage.value = "Veuillez saisir au moins 1 numéro valide entre 0 et 90."
            return
        }
        _userProposedNumbers.value = valid
        _userNumbersAiReport.value = null
        viewModelScope.launch {
            val storedDraws = repository.getStoredDraws(sourceId)
            _userNumbersAnalysis.value = LotoStatsCalculator.analyzeUserProposedNumbers(valid, storedDraws)
        }
    }

    /**
     * Analyse une saisie texte libre (ex: "12, 27, 45" ou "12 27 45") contenant 1 à 5 numéros.
     */
    fun submitUserNumbersInput(rawInput: String): Boolean {
        val tokens = rawInput.trim()
            .split(Regex("[,;\\s\\-/]+"))
            .filter { it.isNotBlank() }

        if (tokens.isEmpty()) {
            if (_userProposedNumbers.value.isNotEmpty()) {
                analyzeUserProposedNumbersFromHistory(_userProposedNumbers.value)
                return true
            }
            snackbarMessage.value = "Entrez 1 à 5 numéros entre 0 et 90 (ex : 12, 27, 45)."
            return false
        }

        val parsed = mutableListOf<Int>()
        for (token in tokens) {
            val value = token.toIntOrNull()
            if (value == null || value !in 0..90) {
                snackbarMessage.value = "Numéro invalide « $token » : utilisez uniquement des chiffres entre 0 et 90."
                return false
            }
            if (!parsed.contains(value)) {
                parsed.add(value)
            }
        }

        val combined = if (parsed.size == 1 && _userProposedNumbers.value.size < 5) {
            (_userProposedNumbers.value + parsed).distinct()
        } else {
            parsed
        }

        if (combined.size > 5) {
            snackbarMessage.value = "Maximum 5 numéros par analyse (seuls les 5 premiers sont retenus)."
        }

        val finalSelection = combined.take(5).sorted()
        val currentDraws = draws.value
        if (currentDraws.isNotEmpty()) {
            analyzeUserProposedNumbers(finalSelection, currentDraws)
        } else {
            analyzeUserProposedNumbersFromHistory(finalSelection)
        }
        return true
    }

    fun removeUserProposedNumber(number: Int) {
        val updated = _userProposedNumbers.value.filter { it != number }
        _userProposedNumbers.value = updated
        _userNumbersAiReport.value = null
        if (updated.isEmpty()) {
            _userNumbersAnalysis.value = null
        } else {
            val currentDraws = draws.value
            if (currentDraws.isNotEmpty()) {
                _userNumbersAnalysis.value = LotoStatsCalculator.analyzeUserProposedNumbers(updated, currentDraws)
            } else {
                analyzeUserProposedNumbersFromHistory(updated)
            }
        }
    }

    fun clearUserProposedNumbers() {
        _userProposedNumbers.value = emptyList()
        _userNumbersAnalysis.value = null
        _userNumbersAiReport.value = null
    }

    fun requestAiAnalysisForUserNumbers() {
        val currentAnalysis = _userNumbersAnalysis.value
        if (currentAnalysis == null) {
            snackbarMessage.value = "Analysez d'abord vos chiffres avant de demander une lecture descriptive."
            return
        }
        viewModelScope.launch {
            isAnalyzingUserNumbersWithAi.value = true
            try {
                val currentSource = sources.value.find { it.id == _selectedSourceId.value }
                val sourceName = currentSource?.name ?: "Niger Loto (CSV intégré)"
                val result = aiService.analyzeUserNumbers(
                    analysis = currentAnalysis,
                    recentDraws = draws.value,
                    sourceName = sourceName
                )
                result.onSuccess { report ->
                    _userNumbersAiReport.value = report
                }.onFailure { err ->
                    snackbarMessage.value = "Erreur IA : ${err.localizedMessage}"
                }
            } finally {
                isAnalyzingUserNumbersWithAi.value = false
            }
        }
    }

    fun saveUserProposedNumbersForTracking() {
        val analysis = _userNumbersAnalysis.value
        if (analysis == null || analysis.numbers.isEmpty()) {
            snackbarMessage.value = "Sélectionnez et analysez vos chiffres d'abord."
            return
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val targetDate = sdf.format(cal.time)

        val entity = ProposedCombination(
            sourceId = _selectedSourceId.value,
            targetDateString = targetDate,
            rank = 1,
            numbersCsv = analysis.numbers.joinToString(","),
            scorePercent = analysis.statisticalScorePercent,
            weightsSummary = "Perso ${analysis.gameTypeLabel}",
            evaluated = false
        )

        viewModelScope.launch {
            repository.saveDailyPropositions(listOf(entity))
            snackbarMessage.value =
                "Sélection (${analysis.numbers.joinToString(" - ")}) archivée pour comparaison le $targetDate."
        }
    }
}
