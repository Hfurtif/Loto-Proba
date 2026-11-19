package com.lotoproba.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente une sélection statistique descriptive suivie dans le temps.
 */
@Entity(tableName = "proposed_combinations")
data class ProposedCombination(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceId: String,
    val targetDateString: String, // Format YYYY-MM-DD
    val rank: Int, // 1 à 7
    val numbersCsv: String, // ex: "7,18,42,65,89"
    val scorePercent: Double, // Indicateur statistique descriptif (en %)
    val weightsSummary: String, // ex: "Réc:40% | Glob:30% | Ret:30%"
    val createdTimestamp: Long = System.currentTimeMillis(),
    val evaluated: Boolean = false,
    val matchedCount: Int = -1, // Nombre de numéros correspondants (0..5) ou -1 si en attente
    val matchedNumbersCsv: String = ""
) {
    fun getNumbersList(): List<Int> {
        return numbersCsv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .sorted()
    }

    fun getMatchedNumbersList(): List<Int> {
        if (matchedNumbersCsv.isBlank()) return emptyList()
        return matchedNumbersCsv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .sorted()
    }
}
