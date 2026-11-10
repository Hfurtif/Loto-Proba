package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente l'une des 5 combinaisons proposées quotidiennement (Étape 5 et 6).
 */
@Entity(tableName = "proposed_combinations")
data class ProposedCombination(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceId: String,
    val targetDateString: String, // La date cible du tirage prévu (YYYY-MM-DD)
    val rank: Int, // 1 à 5
    val numbersCsv: String, // ex: "7,18,42,65,89"
    val scorePercent: Double, // Indicateur statistique descriptif (en %)
    val weightsSummary: String, // ex: "Réc:50% | Glob:30% | Ret:20%"
    val createdTimestamp: Long = System.currentTimeMillis(),
    val evaluated: Boolean = false,
    val matchedCount: Int = -1, // Nombre de numéros sortis (0..5) ou -1 si non encore tiré
    val matchedNumbersCsv: String = "" // Numéros ayant coïncidé
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
