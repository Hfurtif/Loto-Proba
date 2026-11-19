package com.lotoproba.app.data.repository

import android.content.Context
import com.lotoproba.app.data.db.LotoDao
import com.lotoproba.app.data.model.AiEvaluationRecord
import com.lotoproba.app.data.model.LotoDraw
import com.lotoproba.app.data.model.LotoSource
import com.lotoproba.app.data.model.ProposedCombination
import com.lotoproba.app.data.network.DataImporterExporter
import com.lotoproba.app.data.network.ImportReport
import com.lotoproba.app.data.network.SampleDataGenerator
import com.lotoproba.app.data.network.WebScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.IOException

class LotoRepository(
    private val lotoDao: LotoDao,
    private val scraper: WebScraper = WebScraper(),
    private val appContext: Context? = null
) {

    val allSources: Flow<List<LotoSource>> = lotoDao.getAllSourcesFlow()

    fun getDrawsFlow(sourceId: String): Flow<List<LotoDraw>> =
        lotoDao.getDrawsBySourceFlow(sourceId)

    suspend fun getStoredDraws(sourceId: String): List<LotoDraw> = withContext(Dispatchers.IO) {
        lotoDao.getDrawsBySource(sourceId)
    }

    fun getEvaluatedCombinationsFlow(sourceId: String): Flow<List<ProposedCombination>> =
        lotoDao.getEvaluatedCombinationsFlow(sourceId)

    fun getAllCombinationsFlow(sourceId: String): Flow<List<ProposedCombination>> =
        lotoDao.getAllCombinationsFlow(sourceId)

    fun getCombinationsByDateFlow(sourceId: String, date: String): Flow<List<ProposedCombination>> =
        lotoDao.getCombinationsByDateFlow(sourceId, date)

    /**
     * Initialise la base avec la source par défaut basée sur le CSV embarqué (assets/niger_2years.csv).
     */
    suspend fun initializeIfEmpty(context: Context) = withContext(Dispatchers.IO) {
        val defaultSource = SampleDataGenerator.DEFAULT_SOURCES.first()
        val existingSource = lotoDao.getSourceById(defaultSource.id)
        if (existingSource == null) {
            lotoDao.insertSources(SampleDataGenerator.DEFAULT_SOURCES)
            val initialDraws = SampleDataGenerator.loadTwoYearsHistory(context, defaultSource.id)
            lotoDao.insertDraws(initialDraws)
            generateSamplePastPropositions(defaultSource.id, initialDraws)
        } else if (existingSource.url.contains("sites.google.com/view/lotobonheur", ignoreCase = true)) {
            // Migration transparente depuis l'ancienne URL d'exemple vers le CSV embarqué
            lotoDao.updateSource(
                existingSource.copy(
                    name = defaultSource.name,
                    url = SampleDataGenerator.LOCAL_CSV_ASSET_URI,
                    parserPreset = "CSV",
                    lastUpdateStatus = "Données locales intégrées (hors ligne)"
                )
            )
        }
    }

    private suspend fun generateSamplePastPropositions(sourceId: String, draws: List<LotoDraw>) {
        if (draws.size < 6) return
        val pastTargetDraws = draws.take(5)
        val combinationsToInsert = mutableListOf<ProposedCombination>()

        for (targetDraw in pastTargetDraws) {
            val drawNums = targetDraw.getNumbersList()
            for (rank in 1..5) {
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
     * Recharge soit le CSV embarqué (hors ligne), soit la page web distante (avec tolérance aux erreurs et sans effacer les données locales).
     */
    suspend fun refreshFromWebUrl(
        source: LotoSource,
        context: Context? = appContext
    ): Result<ImportReport> = withContext(Dispatchers.IO) {
        if (source.url.startsWith("asset://")) {
            if (context == null) {
                return@withContext Result.failure(
                    IllegalStateException("Contexte indisponible pour relire le fichier CSV intégré.")
                )
            }
            val localDraws = SampleDataGenerator.loadTwoYearsHistory(context, source.id)
            val ids = lotoDao.insertDraws(localDraws)
            val insertedCount = ids.count { it > 0 }
            val duplicateCount = localDraws.size - insertedCount
            evaluatePendingCombinations(source.id)

            val report = ImportReport(
                totalProcessed = localDraws.size,
                validCount = localDraws.size,
                duplicateCount = duplicateCount,
                invalidCount = 0,
                invalidLines = emptyList(),
                parsedDraws = localDraws
            )

            val statusMsg = if (insertedCount > 0) {
                "$insertedCount tirage(s) chargé(s) depuis le CSV intégré"
            } else {
                "À jour (${localDraws.size} tirages locaux vérifiés)"
            }

            lotoDao.updateSource(
                source.copy(
                    lastUpdateTimestamp = System.currentTimeMillis(),
                    lastUpdateStatus = statusMsg
                )
            )
            return@withContext Result.success(report)
        }

        val fetchResult = scraper.fetchUrl(source.url)
        if (fetchResult.isFailure) {
            val ex = fetchResult.exceptionOrNull() ?: IOException("Erreur de connexion à la source.")
            lotoDao.updateSource(
                source.copy(
                    lastUpdateTimestamp = System.currentTimeMillis(),
                    lastUpdateStatus = "Hors ligne / Échec : ${ex.message}"
                )
            )
            return@withContext Result.failure(ex)
        }

        val rawContent = fetchResult.getOrNull().orEmpty()
        val report = scraper.parseDraws(rawContent, source.id, source.parserPreset)

        if (report.parsedDraws.isEmpty()) {
            val msg = "La structure de la page a changé ou aucun tirage valide (5 numéros 0-90) n'a été détecté. Les données locales ont été conservées."
            lotoDao.updateSource(
                source.copy(
                    lastUpdateTimestamp = System.currentTimeMillis(),
                    lastUpdateStatus = "Page non reconnue (données locales conservées)"
                )
            )
            return@withContext Result.failure(IllegalStateException(msg))
        }

        // Insertion avec OnConflictStrategy.IGNORE sur (sourceId, dateString) pour éviter tout doublon
        val ids = lotoDao.insertDraws(report.parsedDraws)
        val insertedCount = ids.count { it > 0 }
        evaluatePendingCombinations(source.id)

        val dbDuplicates = report.parsedDraws.size - insertedCount
        val finalReport = report.copy(duplicateCount = report.duplicateCount + dbDuplicates)

        val statusMsg = if (insertedCount > 0) {
            "$insertedCount nouveau(x) tirage(s) importé(s)"
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
     * Importation manuelle de texte ou CSV avec dédoublonnage par date.
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
        val dbDuplicates = report.parsedDraws.size - insertedCount
        report.copy(duplicateCount = report.duplicateCount + dbDuplicates)
    }

    /**
     * Exporte les tirages de la source en CSV.
     */
    suspend fun exportDrawsToCsv(sourceId: String): String = withContext(Dispatchers.IO) {
        val draws = lotoDao.getDrawsBySource(sourceId)
        DataImporterExporter.exportToCsv(draws)
    }

    /**
     * Compare les combinaisons enregistrées en attente dès qu'un tirage pour cette date existe.
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
     * Enregistre une liste de combinaisons pour une date cible donnée.
     */
    suspend fun saveDailyPropositions(combinations: List<ProposedCombination>) = withContext(Dispatchers.IO) {
        if (combinations.isEmpty()) return@withContext
        val sourceId = combinations[0].sourceId
        val targetDate = combinations[0].targetDateString
        lotoDao.deleteCombinationsForDate(sourceId, targetDate)
        lotoDao.insertCombinations(combinations)
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
        if (id > 0) {
            evaluatePendingCombinations(draw.sourceId)
        }
        id
    }

    suspend fun deleteDraw(draw: LotoDraw) = withContext(Dispatchers.IO) {
        lotoDao.deleteDraw(draw)
    }

    suspend fun resetWithSampleData(
        sourceId: String,
        context: Context? = appContext
    ) = withContext(Dispatchers.IO) {
        lotoDao.deleteAllDrawsForSource(sourceId)
        val draws = if (context != null) {
            SampleDataGenerator.loadTwoYearsHistory(context, sourceId)
        } else {
            SampleDataGenerator.generateHistoricalDraws(sourceId, count = 90)
        }
        lotoDao.insertDraws(draws)
        generateSamplePastPropositions(sourceId, draws)
    }

    fun getAiEvaluationsFlow(sourceId: String): Flow<List<AiEvaluationRecord>> =
        lotoDao.getAllAiEvaluationsFlow(sourceId)

    suspend fun getLatestAiEvaluation(sourceId: String): AiEvaluationRecord? = withContext(Dispatchers.IO) {
        lotoDao.getLatestAiEvaluation(sourceId)
    }

    suspend fun saveAiEvaluation(record: AiEvaluationRecord): Long = withContext(Dispatchers.IO) {
        lotoDao.insertAiEvaluation(record)
    }

    suspend fun updateAiEvaluation(record: AiEvaluationRecord) = withContext(Dispatchers.IO) {
        lotoDao.updateAiEvaluation(record)
    }

    suspend fun deleteAiEvaluation(record: AiEvaluationRecord) = withContext(Dispatchers.IO) {
        lotoDao.deleteAiEvaluation(record)
    }
}
