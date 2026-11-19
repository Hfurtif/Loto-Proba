package com.lotoproba.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente une source locale (CSV intégré) ou une page web de résultats de loto.
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
