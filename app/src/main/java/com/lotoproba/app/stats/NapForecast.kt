package com.lotoproba.app.stats

/**
 * Représente une synthèse statistique groupée au format NAP (NAP 1, NAP 2, NAP 3 ou PERM 5).
 * Statistiques purement descriptives basées sur les tirages passés.
 */
data class NapItem(
    val title: String,
    val code: String,         // "N1", "N2", "N3", "PERM5"
    val numbers: List<Int>,
    val confidencePercent: Int,
    val description: String,
    val ruleExplanation: String
)

data class DailyNapPackage(
    val targetDate: String,
    val mainBankerNap1: NapItem,
    val alternativeNap1: List<Int>,
    val topNap2Pairs: List<NapItem>,
    val topNap3Trios: List<NapItem>,
    val perm5: NapItem
)
