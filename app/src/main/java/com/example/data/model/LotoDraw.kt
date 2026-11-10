package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Représente un tirage journalier de loto composé de 5 numéros distincts compris entre 0 et 90.
 */
@Entity(
    tableName = "loto_draws",
    indices = [
        Index(value = ["sourceId", "dateString", "drawName"], unique = true),
        Index(value = ["dateString"])
    ]
)
data class LotoDraw(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceId: String,
    val dateString: String, // Format YYYY-MM-DD
    val drawName: String = "Tirage du Jour",
    val n1: Int,
    val n2: Int,
    val n3: Int,
    val n4: Int,
    val n5: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Retourne la liste ordonnée des 5 numéros
     */
    fun getNumbersList(): List<Int> = listOf(n1, n2, n3, n4, n5).sorted()

    // Propriétés pratiques d'accès pour l'analyse statistique
    val numbers: List<Int> get() = getNumbersList()
    val date: String get() = dateString

    fun containsNumber(number: Int): Boolean =
        n1 == number || n2 == number || n3 == number || n4 == number || n5 == number

    fun hasNumber(number: Int): Boolean = containsNumber(number)

    /**
     * Vérifie la validité stricte selon les règles du jeu :
     * 5 numéros distincts compris entre 0 et 90 (91 numéros au total).
     */
    fun isValid(): Boolean {
        val list = listOf(n1, n2, n3, n4, n5)
        if (list.size != 5) return false
        if (list.toSet().size != 5) return false
        return list.all { it in 0..90 }
    }
}

/**
 * Typealias 'Draw' pour l'entité Room de tirage de loterie.
 * Supporte le stockage des résultats historiques (date, numéros)
 * pour l'analyse des fréquences, retards et tendances.
 */
typealias Draw = LotoDraw
