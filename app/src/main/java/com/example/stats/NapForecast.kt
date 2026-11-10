package com.example.stats

/**
 * Représente une proposition spécifique de NAP (NAP 1, NAP 2, NAP 3 ou PERM 5).
 * - NAP 1 (N1) : 1 seul chiffre sûr (Banker)
 * - NAP 2 (N2) : 2 chiffres sûrs
 * - NAP 3 (N3) : 3 chiffres sûrs
 * - PERM 5     : Grille complète de 5 numéros
 */
data class NapItem(
    val title: String,        // ex: "NAP 1 (Chiffre Sûr)", "NAP 2 (2 Sûrs)", "NAP 3 (3 Sûrs)"
    val code: String,         // "N1", "N2", "N3", "PERM5"
    val numbers: List<Int>,   // 1 numéro pour N1, 2 pour N2, 3 pour N3, 5 pour PERM5
    val confidencePercent: Int,
    val description: String,  // Justification statistique concise
    val ruleExplanation: String // Rappel de la règle en français simple
)

data class DailyNapPackage(
    val targetDate: String,
    val mainBankerNap1: NapItem,
    val alternativeNap1: List<Int>,
    val topNap2Pairs: List<NapItem>,
    val topNap3Trios: List<NapItem>,
    val perm5: NapItem
)
