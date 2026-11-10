package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.LotoDatabase
import com.example.data.model.LotoDraw
import com.example.data.model.LotoSource
import com.example.data.model.ProposedCombination
import com.example.data.network.ImportReport
import com.example.data.repository.LotoRepository
import com.example.stats.ChiSquareResult
import com.example.stats.GeneratedCombination
import com.example.stats.LotoStatsCalculator
import com.example.stats.DailyNapPackage
import com.example.stats.NapItem
import com.example.stats.NumberStat
import com.example.stats.PairStat
import com.example.stats.TrackingSummary
import com.example.stats.TrioStat
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

import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class LotoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LotoRepository

    init {
        val database = LotoDatabase.getDatabase(application)
        repository = LotoRepository(database.lotoDao())
        viewModelScope.launch {
            repository.initializeIfEmpty(application)
        }
    }

    // Sources
    val sources: StateFlow<List<LotoSource>> = repository.allSources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSourceId = MutableStateFlow("niger_loto")
    val selectedSourceId: StateFlow<String> = _selectedSourceId.asStateFlow()

    // Tirages de la source sélectionnée
    val draws: StateFlow<List<LotoDraw>> = _selectedSourceId.flatMapLatest { sourceId ->
        repository.getDrawsFlow(sourceId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Propositions et suivi
    val allTrackedCombinations: StateFlow<List<ProposedCombination>> = _selectedSourceId.flatMapLatest { sourceId ->
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

    // Pondérations personnalisées pour les 5 combinaisons (Étape 5)
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

    // Statistiques calculées réactivement
    val numberStats: StateFlow<List<NumberStat>> = draws.combine(_selectedSourceId) { list, _ ->
        LotoStatsCalculator.calculateNumberStats(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topPairs: StateFlow<List<PairStat>> = draws.combine(_selectedSourceId) { list, _ ->
        LotoStatsCalculator.calculateTopPairs(list, limit = 10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topTrios: StateFlow<List<TrioStat>> = draws.combine(_selectedSourceId) { list, _ ->
        LotoStatsCalculator.calculateTopTrios(list, limit = 10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chiSquareResult: StateFlow<ChiSquareResult> = draws.combine(_selectedSourceId) { list, _ ->
        LotoStatsCalculator.calculateChiSquareTest(list)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ChiSquareResult(0.0, 90, 113.145, 1.0, true, "Calcul des statistiques en cours...")
    )

    // Combinaisons générées dynamiquement selon les pondérations
    val generatedCombinations: StateFlow<List<GeneratedCombination>> = combine(
        numberStats,
        weightRecent,
        weightGlobal,
        weightGap
    ) { stats, wRecent, wGlobal, wGap ->
        LotoStatsCalculator.generate5ProposedCombinations(stats, wRecent, wGlobal, wGap)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Propositions NAP (NAP 1, NAP 2, NAP 3 et PERM 5)
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
            mainBankerNap1 = NapItem("NAP 1 (Banker)", "N1", listOf(12), 88, "Calcul en cours...", ""),
            alternativeNap1 = listOf(45, 78),
            topNap2Pairs = emptyList(),
            topNap3Trios = emptyList(),
            perm5 = NapItem("PERM 5", "PERM5", listOf(12, 45, 78, 23, 56), 82, "", "")
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
                weightsSummary = "NAP 1 (Banker)",
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
            snackbarMessage.value = "Pronostics NAP 1, NAP 2 et NAP 3 enregistrés pour le $targetDate !"
        }
    }

    fun selectSource(sourceId: String) {
        _selectedSourceId.value = sourceId
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
     * Étape 2 : Rafraîchir depuis l'URL de la source
     */
    fun refreshCurrentSourceWeb() {
        val currentSource = sources.value.find { it.id == _selectedSourceId.value }
        if (currentSource == null) {
            snackbarMessage.value = "Source introuvable."
            return
        }

        viewModelScope.launch {
            isRefreshing.value = true
            val result = repository.refreshFromWebUrl(currentSource)
            isRefreshing.value = false
            result.onSuccess { report ->
                importReport.value = report
                snackbarMessage.value = "Mise à jour réussie : ${report.validCount} tirages analysés (${report.duplicateCount} déjà présents)."
            }.onFailure { ex ->
                snackbarMessage.value = "Échec : ${ex.message}. Vous pouvez utiliser l'import manuel."
            }
        }
    }

    /**
     * Importation manuelle de texte ou CSV
     */
    fun importManualText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            isRefreshing.value = true
            val report = repository.importManualContent(text, _selectedSourceId.value)
            isRefreshing.value = false
            importReport.value = report
            snackbarMessage.value = "Importation terminée : ${report.validCount} tirages valides, ${report.invalidCount} lignes rejetées."
        }
    }

    /**
     * Ajout d'un tirage manuel unitaire avec validation stricte
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
            dateString = dateString,
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
                snackbarMessage.value = "Tirage du $dateString enregistré avec succès."
            } else {
                snackbarMessage.value = "Ce tirage existe déjà pour cette date."
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
     * Enregistre les 5 combinaisons pour le suivi (Étape 6)
     */
    fun saveTodayPropositions() {
        val currentCombos = generatedCombinations.value
        if (currentCombos.isEmpty()) {
            snackbarMessage.value = "Aucune combinaison à enregistrer."
            return
        }

        // Date cible : demain ou aujourd'hui selon disponibilité
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1) // Prévision pour demain
        val targetDate = sdf.format(cal.time)

        val wSummary = "Réc:${weightRecent.value.toInt()}% | Glob:${weightGlobal.value.toInt()}% | Ret:${weightGap.value.toInt()}%"

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
            snackbarMessage.value = "5 combinaisons enregistrées pour le tirage du $targetDate."
        }
    }

    /**
     * Ajout ou modification d'une source (pays / lien URL) - Étape 1
     */
    fun saveSource(name: String, country: String, url: String, parserPreset: String) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isBlank() || name.isBlank()) {
            snackbarMessage.value = "Veuillez renseigner le nom et l'URL."
            return
        }

        val existing = editingSource.value
        val sourceToSave = existing?.copy(
            name = name,
            country = country,
            url = trimmedUrl,
            parserPreset = parserPreset
        ) ?: LotoSource(
            id = "src_" + System.currentTimeMillis(),
            name = name,
            country = country.ifBlank { "International" },
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
        return com.example.data.network.DataImporterExporter.exportToCsv(currentDraws)
    }

    fun resetSampleData() {
        viewModelScope.launch {
            isRefreshing.value = true
            repository.resetWithSampleData(_selectedSourceId.value)
            isRefreshing.value = false
            snackbarMessage.value = "Jeu de données de démonstration réinitialisé (90 tirages)."
        }
    }

    fun clearSnackbar() {
        snackbarMessage.value = null
    }

    fun clearImportReport() {
        importReport.value = null
    }
}
