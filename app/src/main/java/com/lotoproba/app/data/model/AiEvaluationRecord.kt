package com.lotoproba.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant une synthèse analytique générée par l'IA et sa vérification
 * rétrospective lorsque le tirage réel est publié.
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
    val verdictSummary: String? = null,
    val explanationWhy: String? = null,
    val actionAdviceNextDraw: String? = null,
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
