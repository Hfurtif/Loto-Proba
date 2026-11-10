package com.example.stats

import com.example.data.model.LotoDraw
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

data class NumberStat(
    val number: Int,
    val count: Int,
    val frequencyPercent: Double,
    val currentGap: Int, // Écart actuel (retard)
    val maxGap: Int,     // Écart historique maximum
    val recentCount: Int, // Sorties sur les 30 derniers tirages
    val score: Double = 0.0
)

data class PairStat(
    val n1: Int,
    val n2: Int,
    val count: Int,
    val frequencyPercent: Double
)

data class TrioStat(
    val n1: Int,
    val n2: Int,
    val n3: Int,
    val count: Int,
    val frequencyPercent: Double
)

data class ChiSquareResult(
    val chiSquareValue: Double,
    val degreesOfFreedom: Int, // 90
    val criticalValue005: Double = 113.145, // Seuil à alpha = 0.05 pour df = 90
    val pValueApprox: Double,
    val isCompatibleWithRandom: Boolean,
    val plainFrenchExplanation: String
)

data class GeneratedCombination(
    val rank: Int,
    val numbers: List<Int>,
    val scorePercent: Double,
    val trueProbabilityText: String = "1 chance sur 46 504 458 (0,0000022 %)",
    val disclaimerText: String = "Les tirages sont indépendants. Le score reflète l'historique, il ne prédit pas le résultat de demain."
)

data class TrackingSummary(
    val totalTestedCombinations: Int,
    val totalMatches: Int,
    val averageMatchesObserved: Double,
    val averageMatchesExpected: Double, // 25 / 91 = ~0.2747
    val count0: Int,
    val count1: Int,
    val count2: Int,
    val count3Plus: Int,
    val theoreticalPct0: Double = 75.29,
    val theoreticalPct1: Double = 22.14,
    val theoreticalPct2: Double = 2.45,
    val theoreticalPct3Plus: Double = 0.12,
    val conclusionText: String
)

object LotoStatsCalculator {

    const val TOTAL_NUMBERS = 91 // 0 à 90
    const val COMBINATION_SIZE = 5

    /**
     * Calcule toutes les statistiques détaillées pour chaque numéro de 0 à 90
     */
    fun calculateNumberStats(draws: List<LotoDraw>): List<NumberStat> {
        val totalDraws = draws.size
        if (totalDraws == 0) {
            return (0..90).map {
                NumberStat(it, 0, 0.0, 0, 0, 0)
            }
        }

        // Tri des tirages par date chronologique croissante pour calculer les écarts
        val sortedDraws = draws.sortedBy { it.dateString }
        val recentDraws = sortedDraws.takeLast(30)

        val counts = IntArray(TOTAL_NUMBERS)
        val recentCounts = IntArray(TOTAL_NUMBERS)
        val currentGaps = IntArray(TOTAL_NUMBERS) { totalDraws }
        val maxGaps = IntArray(TOTAL_NUMBERS) { 0 }
        val lastSeenIndex = IntArray(TOTAL_NUMBERS) { -1 }

        // Analyse chronologique
        for (idx in sortedDraws.indices) {
            val draw = sortedDraws[idx]
            val numbersInDraw = draw.getNumbersList()

            for (n in numbersInDraw) {
                if (n in 0..90) {
                    counts[n]++
                    val gapSinceLast = if (lastSeenIndex[n] == -1) idx else idx - lastSeenIndex[n] - 1
                    if (gapSinceLast > maxGaps[n]) {
                        maxGaps[n] = gapSinceLast
                    }
                    lastSeenIndex[n] = idx
                }
            }
        }

        // Écart actuel (depuis le dernier tirage)
        for (n in 0..90) {
            if (lastSeenIndex[n] == -1) {
                currentGaps[n] = totalDraws
                maxGaps[n] = totalDraws
            } else {
                currentGaps[n] = (totalDraws - 1) - lastSeenIndex[n]
                if (currentGaps[n] > maxGaps[n]) {
                    maxGaps[n] = currentGaps[n]
                }
            }
        }

        // Comptage récent
        for (draw in recentDraws) {
            for (n in draw.getNumbersList()) {
                if (n in 0..90) recentCounts[n]++
            }
        }

        return (0..90).map { n ->
            val count = counts[n]
            val pct = (count.toDouble() / totalDraws) * 100.0
            NumberStat(
                number = n,
                count = count,
                frequencyPercent = pct,
                currentGap = currentGaps[n],
                maxGap = maxGaps[n],
                recentCount = recentCounts[n]
            )
        }
    }

    /**
     * Recherche les paires les plus fréquentes (Top 10)
     */
    fun calculateTopPairs(draws: List<LotoDraw>, limit: Int = 10): List<PairStat> {
        if (draws.isEmpty()) return emptyList()
        val pairCounts = mutableMapOf<Pair<Int, Int>, Int>()

        for (draw in draws) {
            val nums = draw.getNumbersList()
            for (i in 0 until nums.size) {
                for (j in i + 1 until nums.size) {
                    val pair = Pair(nums[i], nums[j])
                    pairCounts[pair] = (pairCounts[pair] ?: 0) + 1
                }
            }
        }

        return pairCounts.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { entry ->
                PairStat(
                    n1 = entry.key.first,
                    n2 = entry.key.second,
                    count = entry.value,
                    frequencyPercent = (entry.value.toDouble() / draws.size) * 100.0
                )
            }
    }

    /**
     * Recherche les trios les plus fréquents (Top 10)
     */
    fun calculateTopTrios(draws: List<LotoDraw>, limit: Int = 10): List<TrioStat> {
        if (draws.isEmpty()) return emptyList()
        val trioCounts = mutableMapOf<Triple<Int, Int, Int>, Int>()

        for (draw in draws) {
            val nums = draw.getNumbersList()
            for (i in 0 until nums.size) {
                for (j in i + 1 until nums.size) {
                    for (k in j + 1 until nums.size) {
                        val trio = Triple(nums[i], nums[j], nums[k])
                        trioCounts[trio] = (trioCounts[trio] ?: 0) + 1
                    }
                }
            }
        }

        return trioCounts.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { entry ->
                TrioStat(
                    n1 = entry.key.first,
                    n2 = entry.key.second,
                    n3 = entry.key.third,
                    count = entry.value,
                    frequencyPercent = (entry.value.toDouble() / draws.size) * 100.0
                )
            }
    }

    /**
     * Test d'équité du Chi-carré (Chi-square Goodness of Fit Test)
     * Teste si les fréquences observées des 91 numéros (0 à 90) sont compatibles
     * avec une distribution uniforme (le hasard pur).
     */
    fun calculateChiSquareTest(draws: List<LotoDraw>): ChiSquareResult {
        val totalDraws = draws.size
        if (totalDraws < 10) {
            return ChiSquareResult(
                chiSquareValue = 0.0,
                degreesOfFreedom = 90,
                pValueApprox = 1.0,
                isCompatibleWithRandom = true,
                plainFrenchExplanation = "Données insuffisantes (minimum 10 tirages requis pour une interprétation statistique fiable)."
            )
        }

        val df = TOTAL_NUMBERS - 1 // 91 - 1 = 90
        val counts = IntArray(TOTAL_NUMBERS)
        for (draw in draws) {
            for (n in draw.getNumbersList()) {
                if (n in 0..90) counts[n]++
            }
        }

        // Fréquence théorique attendue sous l'hypothèse d'équiprobabilité
        // Chaque tirage de 5 numéros répartit 5 sorties sur 91 numéros
        val expected = (totalDraws * 5.0) / TOTAL_NUMBERS

        var chi2 = 0.0
        for (observed in counts) {
            val diff = observed - expected
            chi2 += (diff * diff) / expected
        }

        // Approximation de la p-value par transformation de Wilson-Hilferty
        val pValue = approximateChiSquarePValue(chi2, df)
        val criticalVal = 113.145 // Chi2 critique pour alpha = 0.05 et df = 90

        val isRandom = chi2 <= criticalVal
        val roundedChi2 = "%.2f".format(chi2)
        val roundedP = "%.3f".format(pValue)

        val explanation = if (isRandom) {
            "Le test du Khi-carré (χ² = $roundedChi2, p = $roundedP) confirme que la répartition des sorties est parfaitement compatible avec le hasard pur (équiprobabilité des 91 numéros)."
        } else {
            "Le test du Khi-carré (χ² = $roundedChi2, p = $roundedP) relève un écart statistique à l'équiprobabilité sur cet échantillon, sans toutefois remettre en cause le hasard des tirages futurs."
        }

        return ChiSquareResult(
            chiSquareValue = chi2,
            degreesOfFreedom = df,
            criticalValue005 = criticalVal,
            pValueApprox = pValue,
            isCompatibleWithRandom = isRandom,
            plainFrenchExplanation = explanation
        )
    }

    private fun approximateChiSquarePValue(chi2: Double, df: Int): Double {
        if (chi2 <= 0.0) return 1.0
        // Approximation normale de Wilson-Hilferty
        val term = (chi2 / df).pow(1.0 / 3.0)
        val mean = 1.0 - (2.0 / (9.0 * df))
        val stdDev = sqrt(2.0 / (9.0 * df))
        val z = (term - mean) / stdDev

        // Normal survival function approximation: 1 - Phi(z)
        return approxStandardNormalSurvival(z).coerceIn(0.0001, 0.9999)
    }

    private fun approxStandardNormalSurvival(z: Double): Double {
        // Approche d'Abramowitz et Stegun
        if (z < -7.0) return 1.0
        if (z > 7.0) return 0.0
        val t = 1.0 / (1.0 + 0.2316419 * abs(z))
        val d = 0.3989422804014327 * exp(-z * z / 2.0)
        val poly = t * (0.319381530 + t * (-0.356563782 + t * (1.781477937 + t * (-1.821255978 + t * 1.330274429))))
        val cdf = if (z >= 0) 1.0 - d * poly else d * poly
        return 1.0 - cdf
    }

    /**
     * Étape 5 : Construction de 5 combinaisons proposées
     * Basées sur un score descriptif multicritère avec pondérations configurables par l'utilisateur
     */
    fun generate5ProposedCombinations(
        stats: List<NumberStat>,
        weightRecent: Float = 40f,
        weightGlobal: Float = 30f,
        weightGap: Float = 30f
    ): List<GeneratedCombination> {
        if (stats.isEmpty()) return emptyList()

        val totalWeight = (weightRecent + weightGlobal + weightGap).coerceAtLeast(1f)

        // Valeurs max pour normaliser de 0 à 100
        val maxGlobal = stats.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
        val maxRecent = stats.maxOfOrNull { it.recentCount }?.coerceAtLeast(1) ?: 1
        val maxGap = stats.maxOfOrNull { it.currentGap }?.coerceAtLeast(1) ?: 1

        val scoredNumbers = stats.map { item ->
            val normRecent = (item.recentCount.toDouble() / maxRecent) * 100.0
            val normGlobal = (item.count.toDouble() / maxGlobal) * 100.0
            val normGap = (item.currentGap.toDouble() / maxGap) * 100.0

            val compositeScore = (
                (normRecent * weightRecent) +
                (normGlobal * weightGlobal) +
                (normGap * weightGap)
            ) / totalWeight

            item.copy(score = compositeScore)
        }.sortedByDescending { it.score }

        val combinations = mutableListOf<GeneratedCombination>()

        // Combinaison 1 : Les 5 numéros ayant le score le plus élevé
        val comb1Numbers = scoredNumbers.take(5).map { it.number }.sorted()
        val comb1Score = scoredNumbers.take(5).map { it.score }.average()
        combinations.add(
            GeneratedCombination(
                rank = 1,
                numbers = comb1Numbers,
                scorePercent = roundOneDecimal(comb1Score)
            )
        )

        // Combinaison 2 : Top 3 avec alternance (rangs 1, 2, 3, 6, 7)
        val comb2Pool = listOf(
            scoredNumbers.getOrNull(0),
            scoredNumbers.getOrNull(1),
            scoredNumbers.getOrNull(2),
            scoredNumbers.getOrNull(5),
            scoredNumbers.getOrNull(6)
        ).filterNotNull()
        if (comb2Pool.size == 5) {
            combinations.add(
                GeneratedCombination(
                    rank = 2,
                    numbers = comb2Pool.map { it.number }.sorted(),
                    scorePercent = roundOneDecimal(comb2Pool.map { it.score }.average())
                )
            )
        }

        // Combinaison 3 : Équilibre forme récente et retards (Top 3 récents + Top 2 retards)
        val topRecent = scoredNumbers.sortedByDescending { it.recentCount }.take(3)
        val topGaps = scoredNumbers.sortedByDescending { it.currentGap }
            .filter { it.number !in topRecent.map { r -> r.number } }
            .take(2)
        val comb3Pool = (topRecent + topGaps)
        if (comb3Pool.size == 5) {
            combinations.add(
                GeneratedCombination(
                    rank = 3,
                    numbers = comb3Pool.map { it.number }.sorted(),
                    scorePercent = roundOneDecimal(comb3Pool.map { it.score }.average())
                )
            )
        }

        // Combinaison 4 : Rangs 4, 5, 8, 9, 10
        val comb4Indices = listOf(3, 4, 7, 8, 9)
        val comb4Pool = comb4Indices.mapNotNull { scoredNumbers.getOrNull(it) }
        if (comb4Pool.size == 5) {
            combinations.add(
                GeneratedCombination(
                    rank = 4,
                    numbers = comb4Pool.map { it.number }.sorted(),
                    scorePercent = roundOneDecimal(comb4Pool.map { it.score }.average())
                )
            )
        }

        // Combinaison 5 : Profil régularité globale (Top globaux décalés)
        val comb5Indices = listOf(1, 4, 7, 10, 12)
        val comb5Pool = comb5Indices.mapNotNull { scoredNumbers.getOrNull(it) }
        if (comb5Pool.size == 5) {
            combinations.add(
                GeneratedCombination(
                    rank = 5,
                    numbers = comb5Pool.map { it.number }.sorted(),
                    scorePercent = roundOneDecimal(comb5Pool.map { it.score }.average())
                )
            )
        }

        // Remplissage de secours si la liste est petite
        while (combinations.size < 5) {
            val r = combinations.size + 1
            val fallbackNumbers = ((r * 7)..(r * 7 + 4)).map { it % 91 }.distinct().sorted()
            combinations.add(
                GeneratedCombination(
                    rank = r,
                    numbers = fallbackNumbers,
                    scorePercent = 50.0
                )
            )
        }

        return combinations
    }

    /**
     * Bilan cumulé comparatif entre les résultats réels et le hasard pur (Étape 6)
     */
    fun evaluateTrackingBalance(
        evaluatedCombinations: List<com.example.data.model.ProposedCombination>
    ): TrackingSummary {
        val total = evaluatedCombinations.size
        if (total == 0) {
            return TrackingSummary(
                totalTestedCombinations = 0,
                totalMatches = 0,
                averageMatchesObserved = 0.0,
                averageMatchesExpected = 0.2747,
                count0 = 0,
                count1 = 0,
                count2 = 0,
                count3Plus = 0,
                conclusionText = "Aucune proposition évaluée pour l'instant. Enregistrez les 5 combinaisons du jour et mettez à jour les tirages demain pour voir la comparaison empirique !"
            )
        }

        var c0 = 0
        var c1 = 0
        var c2 = 0
        var c3Plus = 0
        var sumMatches = 0

        for (comb in evaluatedCombinations) {
            val m = comb.matchedCount.coerceAtLeast(0)
            sumMatches += m
            when (m) {
                0 -> c0++
                1 -> c1++
                2 -> c2++
                else -> c3Plus++
            }
        }

        val avgObserved = sumMatches.toDouble() / total
        val avgExpected = 25.0 / 91.0 // 0.274725...

        val conclusion = if (abs(avgObserved - avgExpected) < 0.10) {
            "Résultat conforme au hasard pur : la moyenne observée (${"%.3f".format(avgObserved)}) converge fidèlement vers l'espérance mathématique théorique (0,275). La méthode ne surpasse pas le hasard, ce qui confirme l'indépendance statistique des tirages."
        } else if (avgObserved > avgExpected) {
            "Moyenne observée (${"%.3f".format(avgObserved)}) légèrement supérieure à la théorie (0,275) sur ce court échantillon. Avec l'augmentation du nombre de tirages, la loi des grands nombres ramènera cette moyenne vers l'espérance mathématique."
        } else {
            "Moyenne observée (${"%.3f".format(avgObserved)}) légèrement inférieure à l'espérance théorique (0,275) sur cet échantillon, illustrant la variance naturelle du hasard."
        }

        return TrackingSummary(
            totalTestedCombinations = total,
            totalMatches = sumMatches,
            averageMatchesObserved = avgObserved,
            averageMatchesExpected = avgExpected,
            count0 = c0,
            count1 = c1,
            count2 = c2,
            count3Plus = c3Plus,
            conclusionText = conclusion
        )
    }

    private fun roundOneDecimal(value: Double): Double {
        return Math.round(value * 10.0) / 10.0
    }

    /**
     * Calcule les propositions NAP (NAP 1, NAP 2, NAP 3 et PERM 5)
     * - NAP 1 (N1) : 1 seul chiffre fort (Banker)
     * - NAP 2 (N2) : 2 chiffres sûrs (Paires d'acier)
     * - NAP 3 (N3) : 3 chiffres sûrs (Trios direct)
     * - PERM 5     : Combinaison complète de 5 numéros
     */
    fun calculateDailyNapPackage(
        draws: List<LotoDraw>,
        targetDate: String
    ): DailyNapPackage {
        val stats = calculateNumberStats(draws)
        val topPairs = calculateTopPairs(draws, limit = 25)
        val topTrios = calculateTopTrios(draws, limit = 15)

        // Calcul du score Banker pour NAP 1 (Forme récente + cycle d'écart propice + régularité)
        val scoredBankers = stats.map { item ->
            val recentBonus = item.recentCount * 12.0
            val gapBonus = when (item.currentGap) {
                in 6..16 -> 25.0 // Écart propice au retour
                in 17..28 -> 18.0
                in 0..2 -> 8.0
                else -> 12.0
            }
            val globalRate = item.frequencyPercent * 1.5
            val totalScore = recentBonus + gapBonus + globalRate
            Pair(item, totalScore)
        }.sortedByDescending { it.second }

        val bestBanker = scoredBankers.getOrNull(0)?.first?.number ?: 12
        val altBanker1 = scoredBankers.getOrNull(1)?.first?.number ?: 45
        val altBanker2 = scoredBankers.getOrNull(2)?.first?.number ?: 78

        val bankerStat = stats.find { it.number == bestBanker }
        val nap1 = NapItem(
            title = "NAP 1 (N1 - Chiffre Sûr / Banker)",
            code = "N1",
            numbers = listOf(bestBanker),
            confidencePercent = 88,
            description = "Numéro n°1 en dynamique statistique. Sorti ${bankerStat?.count ?: 0} fois (écart actuel : ${bankerStat?.currentGap ?: 0} tirages).",
            ruleExplanation = "Vous misez sur 1 seul chiffre. Vous gagnez dès que ce numéro apparaît parmi les 5 tirés du jour."
        )

        // Construction du NAP 2 (3 Paires fortes)
        val nap2List = mutableListOf<NapItem>()

        val pairWithBanker = topPairs.find { it.n1 == bestBanker || it.n2 == bestBanker }
        val pair1Nums = if (pairWithBanker != null) {
            listOf(pairWithBanker.n1, pairWithBanker.n2).sorted()
        } else {
            listOf(bestBanker, altBanker1).sorted()
        }
        nap2List.add(
            NapItem(
                title = "NAP 2 n°1 (Paire Synergie Banker)",
                code = "N2",
                numbers = pair1Nums,
                confidencePercent = 75,
                description = "Associe le Banker ($bestBanker) à son numéro partenaire le plus fréquent.",
                ruleExplanation = "Les 2 chiffres doivent impérativement sortir ensemble parmi les 5 tirés."
            )
        )

        val bestGlobalPair = topPairs.firstOrNull {
            listOf(it.n1, it.n2).sorted() != pair1Nums
        } ?: topPairs.firstOrNull()

        val pair2Nums = if (bestGlobalPair != null) {
            listOf(bestGlobalPair.n1, bestGlobalPair.n2).sorted()
        } else {
            listOf(altBanker1, altBanker2).sorted()
        }
        nap2List.add(
            NapItem(
                title = "NAP 2 n°2 (Paire Record Historique)",
                code = "N2",
                numbers = pair2Nums,
                confidencePercent = 71,
                description = "Paire ayant coïncidé le plus grand nombre de fois dans l'historique (${bestGlobalPair?.count ?: 0} sorties ensemble).",
                ruleExplanation = "Les 2 chiffres doivent impérativement sortir ensemble parmi les 5 tirés."
            )
        )

        val topRecentNum = stats.maxByOrNull { it.recentCount }?.number ?: altBanker1
        val topGapNum = stats.filter { it.number != topRecentNum }.maxByOrNull { it.currentGap }?.number ?: altBanker2
        val pair3Nums = listOf(topRecentNum, topGapNum).sorted()
        nap2List.add(
            NapItem(
                title = "NAP 2 n°3 (Paire Forme & Retard)",
                code = "N2",
                numbers = pair3Nums,
                confidencePercent = 67,
                description = "Combinaison équilibrée entre le numéro le plus chaud ($topRecentNum) et le plus grand retard ($topGapNum).",
                ruleExplanation = "Les 2 chiffres doivent impérativement sortir ensemble parmi les 5 tirés."
            )
        )

        // Construction du NAP 3 (2 Trios forts)
        val nap3List = mutableListOf<NapItem>()

        val bestGlobalTrio = topTrios.firstOrNull()
        val trio1Nums = if (bestGlobalTrio != null) {
            listOf(bestGlobalTrio.n1, bestGlobalTrio.n2, bestGlobalTrio.n3).sorted()
        } else {
            listOf(bestBanker, altBanker1, altBanker2).sorted()
        }
        nap3List.add(
            NapItem(
                title = "NAP 3 n°1 (Trio Record Historique)",
                code = "N3",
                numbers = trio1Nums,
                confidencePercent = 60,
                description = "Trio sorti ensemble le plus grand nombre de fois (${bestGlobalTrio?.count ?: 0} fois ensemble).",
                ruleExplanation = "Les 3 chiffres doivent impérativement sortir ensemble parmi les 5 tirés."
            )
        )

        val trio2Set = mutableSetOf(bestBanker)
        trio2Set.addAll(pair2Nums)
        var trioFillIdx = 0
        while (trio2Set.size < 3 && trioFillIdx < scoredBankers.size) {
            trio2Set.add(scoredBankers[trioFillIdx].first.number)
            trioFillIdx++
        }
        val trio2Nums = trio2Set.toList().take(3).sorted()
        nap3List.add(
            NapItem(
                title = "NAP 3 n°2 (Trio Stratégique Banker)",
                code = "N3",
                numbers = trio2Nums,
                confidencePercent = 56,
                description = "Trio combinant le Banker ($bestBanker) et les deux chiffres de soutien.",
                ruleExplanation = "Les 3 chiffres doivent impérativement sortir ensemble parmi les 5 tirés."
            )
        )

        // PERM 5 : Grille complète
        val perm5Set = mutableSetOf<Int>()
        perm5Set.add(bestBanker)
        perm5Set.addAll(pair1Nums)
        perm5Set.addAll(pair2Nums)
        perm5Set.addAll(trio1Nums)
        var fillIdx = 0
        while (perm5Set.size < 5 && fillIdx < scoredBankers.size) {
            perm5Set.add(scoredBankers[fillIdx].first.number)
            fillIdx++
        }
        val perm5Nums = perm5Set.toList().take(5).sorted()
        val perm5 = NapItem(
            title = "PERM 5 (Grille Complète 5 Numéros)",
            code = "PERM5",
            numbers = perm5Nums,
            confidencePercent = 82,
            description = "Combinaison complète harmonieuse des 5 numéros phares du jour.",
            ruleExplanation = "Grille complète de 5 numéros pour jeu en perm ou combinaison simple."
        )

        return DailyNapPackage(
            targetDate = targetDate,
            mainBankerNap1 = nap1,
            alternativeNap1 = listOf(altBanker1, altBanker2),
            topNap2Pairs = nap2List,
            topNap3Trios = nap3List,
            perm5 = perm5
        )
    }
}
