package com.example.data.repository

import com.example.data.db.LotoDao
import com.example.data.model.LotoDraw
import com.example.data.model.LotoSource
import com.example.data.model.ProposedCombination
import com.example.data.network.DataImporterExporter
import com.example.data.network.ImportReport
import com.example.data.network.SampleDataGenerator
import com.example.data.network.WebScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LotoRepository(
    private val lotoDao: LotoDao,
    private val scraper: WebScraper = WebScraper()
) {

    val allSources: Flow<List<LotoSource>> = lotoDao.getAllSourcesFlow()

    fun getDrawsFlow(sourceId: String): Flow<List<LotoDraw>> =
        lotoDao.getDrawsBySourceFlow(sourceId)

    fun getEvaluatedCombinationsFlow(sourceId: String): Flow<List<ProposedCombination>> =
        lotoDao.getEvaluatedCombinationsFlow(sourceId)

    fun getAllCombinationsFlow(sourceId: String): Flow<List<ProposedCombination>> =
        lotoDao.getAllCombinationsFlow(sourceId)

    fun getCombinationsByDateFlow(sourceId: String, date: String): Flow<List<ProposedCombination>> =
        lotoDao.getCombinationsByDateFlow(sourceId, date)

    /**
     * Initialise la base avec les sources par défaut et un historique complet sur 2 ans (720+ tirages)
     */
    suspend fun initializeIfEmpty(context: android.content.Context) = withContext(Dispatchers.IO) {
        val existingSources = lotoDao.getSourceById(SampleDataGenerator.DEFAULT_SOURCES[0].id)
        if (existingSources == null) {
            // Insérer les sources
            lotoDao.insertSources(SampleDataGenerator.DEFAULT_SOURCES)

            // Charger l'historique sur 2 ans pour le Niger (722 tirages réels et étendus)
            val primarySource = SampleDataGenerator.DEFAULT_SOURCES[0]
            val initialDraws = SampleDataGenerator.loadTwoYearsHistory(context, primarySource.id)
            lotoDao.insertDraws(initialDraws)

            // Créer des propositions passées pour la démonstration immédiate du suivi NAP
            generateSamplePastPropositions(primarySource.id, initialDraws)
        }
    }

    private suspend fun generateSamplePastPropositions(sourceId: String, draws: List<LotoDraw>) {
        if (draws.size < 6) return
        val pastTargetDraws = draws.take(5) // Les 5 plus récents
        val combinationsToInsert = mutableListOf<ProposedCombination>()

        for (targetDraw in pastTargetDraws) {
            val drawNums = targetDraw.getNumbersList()
            for (rank in 1..5) {
                // Simuler des propositions plausibles
                val pool = (0..90).shuffled().take(5).sorted()
                val matches = pool.intersect(drawNums.toSet()).toList()
                combinationsToInsert.add(
                    ProposedCombination(
                        sourceId = sourceId,
                        targetDateString = targetDraw.dateString,
                        rank = rank,
                        numbersCsv = pool.joinToString(","),
                        scorePercent = 65.0 + (rank * 2.5),
                        weightsSummary = "Réc:40% | Glob:30% | Ret:30%",
                        evaluated = true,
                        matchedCount = matches.size,
                        matchedNumbersCsv = matches.joinToString(",")
                    )
                )
            }
        }
        lotoDao.insertCombinations(combinationsToInsert)
    }

    /**
     * Étape 2 : Récupération automatique depuis l'URL de la source
     */
    suspend fun refreshFromWebUrl(source: LotoSource): Result<ImportReport> = withContext(Dispatchers.IO) {
        val fetchResult = scraper.fetchUrl(source.url)
        if (fetchResult.isFailure) {
            val ex = fetchResult.exceptionOrNull() ?: Exception("Erreur de connexion à l'URL")
            lotoDao.updateSource(
                source.copy(
                    lastUpdateTimestamp = System.currentTimeMillis(),
                    lastUpdateStatus = "Échec : ${ex.message}"
                )
            )
            return@withContext Result.failure(ex)
        }

        val rawContent = fetchResult.getOrNull() ?: ""
        val report = scraper.parseDraws(rawContent, source.id, source.parserPreset)

        var insertedCount = 0
        if (report.parsedDraws.isNotEmpty()) {
            val isFirstSync = source.lastUpdateTimestamp == null || source.lastUpdateStatus.contains("Non synchronisé", ignoreCase = true)
            if (isFirstSync) {
                // Remplacer les données de démonstration par les vrais résultats officiels téléchargés
                lotoDao.deleteAllDrawsForSource(source.id)
            }

            val ids = lotoDao.insertDraws(report.parsedDraws)
            insertedCount = ids.count { it > 0 }
            evaluatePendingCombinations(source.id)
        }

        val finalReport = report.copy(duplicateCount = report.parsedDraws.size - insertedCount)

        val statusMsg = if (insertedCount > 0) {
            "$insertedCount tirage(s) officiel(s) enregistré(s)"
        } else {
            "À jour (${report.parsedDraws.size} tirages vérifiés)"
        }

        lotoDao.updateSource(
            source.copy(
                lastUpdateTimestamp = System.currentTimeMillis(),
                lastUpdateStatus = statusMsg
            )
        )

        Result.success(finalReport)
    }

    /**
     * Importation manuelle de texte ou CSV
     */
    suspend fun importManualContent(
        content: String,
        sourceId: String
    ): ImportReport = withContext(Dispatchers.IO) {
        val report = DataImporterExporter.parseCsv(content, sourceId)
        var insertedCount = 0
        if (report.parsedDraws.isNotEmpty()) {
            val ids = lotoDao.insertDraws(report.parsedDraws)
            insertedCount = ids.count { it > 0 }
            evaluatePendingCombinations(sourceId)
        }
        report.copy(duplicateCount = report.parsedDraws.size - insertedCount)
    }

    /**
     * Exporte les tirages de la source en CSV
     */
    suspend fun exportDrawsToCsv(sourceId: String): String = withContext(Dispatchers.IO) {
        val draws = lotoDao.getDrawsBySource(sourceId)
        DataImporterExporter.exportToCsv(draws)
    }

    /**
     * Vérifie et évalue les propositions quotidiennes en attente de résultat
     */
    suspend fun evaluatePendingCombinations(sourceId: String) = withContext(Dispatchers.IO) {
        val pending = lotoDao.getPendingCombinations().filter { it.sourceId == sourceId }
        for (comb in pending) {
            val actualDraw = lotoDao.getDrawByDate(comb.sourceId, comb.targetDateString)
            if (actualDraw != null) {
                val proposedNumbers = comb.getNumbersList()
                val drawnNumbers = actualDraw.getNumbersList()
                val matched = proposedNumbers.intersect(drawnNumbers.toSet()).sorted()

                lotoDao.updateCombination(
                    comb.copy(
                        evaluated = true,
                        matchedCount = matched.size,
                        matchedNumbersCsv = matched.joinToString(",")
                    )
                )
            }
        }
    }

    /**
     * Enregistre les 5 combinaisons pour une date cible donnée (Étape 6)
     */
    suspend fun saveDailyPropositions(combinations: List<ProposedCombination>) = withContext(Dispatchers.IO) {
        if (combinations.isEmpty()) return@withContext
        val sourceId = combinations[0].sourceId
        val targetDate = combinations[0].targetDateString
        // Supprimer d'éventuelles propositions existantes pour cette même date et source
        lotoDao.deleteCombinationsForDate(sourceId, targetDate)
        lotoDao.insertCombinations(combinations)
        // Vérifier si le tirage est déjà connu
        evaluatePendingCombinations(sourceId)
    }

    suspend fun insertOrUpdateSource(source: LotoSource) = withContext(Dispatchers.IO) {
        lotoDao.insertSource(source)
    }

    suspend fun deleteSource(source: LotoSource) = withContext(Dispatchers.IO) {
        lotoDao.deleteSource(source)
        lotoDao.deleteAllDrawsForSource(source.id)
    }

    suspend fun insertDraw(draw: LotoDraw): Long = withContext(Dispatchers.IO) {
        val id = lotoDao.insertDraw(draw)
        evaluatePendingCombinations(draw.sourceId)
        id
    }

    suspend fun deleteDraw(draw: LotoDraw) = withContext(Dispatchers.IO) {
        lotoDao.deleteDraw(draw)
    }

    suspend fun resetWithSampleData(sourceId: String) = withContext(Dispatchers.IO) {
        lotoDao.deleteAllDrawsForSource(sourceId)
        val draws = SampleDataGenerator.generateHistoricalDraws(sourceId, count = 90)
        lotoDao.insertDraws(draws)
        generateSamplePastPropositions(sourceId, draws)
    }
}
