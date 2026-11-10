package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente une source ou pays de résultats de loto avec son URL web.
 * Étape 1 : Connexion et Lien
 */
@Entity(tableName = "loto_sources")
data class LotoSource(
    @PrimaryKey
    val id: String,
    val name: String,
    val country: String,
    val url: String,
    val parserPreset: String = "AUTO", // AUTO, HTML_TABLE, TEXT_REGEX, CSV
    val lastUpdateTimestamp: Long? = null,
    val lastUpdateStatus: String = "Non synchronisé",
    val isActive: Boolean = true
)
