package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant une prédiction générée par l'IA et son évaluation
 * rétrospective lorsque le tirage réel est sorti (analyse si l'IA a eu raison ou tort
 * et conseils sur ce qu'il faut faire pour le tirage suivant).
 */
@Entity(tableName = "ai_evaluations")
data class AiEvaluationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceId: String,
    val dateGenerated: String,
    val targetDate: String,
    val bankerN1: Int,
    val nap2PairsCsv: String, // ex: "14-52,07-33"
    val nap3TriosCsv: String, // ex: "14-52-78"
    val perm5Csv: String,     // ex: "14,52,07,33,78"
    val aiReasoning: String,
    val confidenceScore: Int = 85,
    val evaluated: Boolean = false,
    val actualDrawDate: String? = null,
    val actualWinningNumbersCsv: String? = null,
    val verdictSummary: String? = null,        // Ex: "🟢 SUCCÈS NAP 1 ! Le Banker 14 est sorti."
    val explanationWhy: String? = null,        // Analyse des numéros sortis
    val actionAdviceNextDraw: String? = null,  // Que faut-il faire dans ce cas pour le prochain tirage ?
    val matchedCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getPerm5List(): List<Int> =
        perm5Csv.split(",").mapNotNull { it.trim().toIntOrNull() }

    fun getActualWinningList(): List<Int> =
        actualWinningNumbersCsv?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()

    fun getNap2List(): List<List<Int>> =
        nap2PairsCsv.split(",").map { pairStr ->
            pairStr.split("-").mapNotNull { it.trim().toIntOrNull() }
        }.filter { it.size == 2 }

    fun getNap3List(): List<List<Int>> =
        nap3TriosCsv.split(",").map { trioStr ->
            trioStr.split("-").mapNotNull { it.trim().toIntOrNull() }
        }.filter { it.size == 3 }
}
