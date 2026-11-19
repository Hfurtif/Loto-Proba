package com.lotoproba.app.stats

import com.lotoproba.app.data.model.LotoDraw
import com.lotoproba.app.data.model.ProposedCombination
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

data class NumberStat(
    val number: Int,
    val count: Int,
    val frequencyPercent: Double,
    val currentGap: Int,
    val maxGap: Int,
    val recentCount: Int,
    val score: Double = 0.0
)

data class NumberFrequency(
    val number: Int,
    val formattedNumber: String, // "01" à "90"
    val count: Int,
    val frequencyPercent: Double
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

data class PairsAndTriosAnalysis(
    val totalDrawsAnalyzed: Int,
    val topPairs: List<PairStat>,
    val topTrios: List<TrioStat>
)

data class NumberGapStat(
    val number: Int,
    val formattedNumber: String,
    val drawsSinceLastAppearance: Int,
    val maxGap: Int,
    val lastSeenDate: String?,
    val recentAppearances: Int,
    val totalAppearances: Int,
    val neverAppeared: Boolean
)

data class GapAndColdAnalysis(
    val totalDrawsAnalyzed: Int,
    val recentWindowSize: Int,
    val numberGaps: List<NumberGapStat>,
    val longestGaps: List<NumberGapStat>,
    val leastRecentlyDrawn: List<NumberGapStat>
)

data class UserNumberDetail(
    val number: Int,
    val formattedNumber: String,
    val totalCount: Int,
    val frequencyPercent: Double,
    val recentCount: Int,
    val currentGap: Int,
    val maxGap: Int,
    val individualScorePercent: Double,
    val trendLabel: String
)

data class UserPairSynergy(
    val n1: Int,
    val n2: Int,
    val coOccurrenceCount: Int,
    val coOccurrencePercent: Double
)

data class UserNumbersAnalysis(
    val numbers: List<Int>,
    val gameTypeLabel: String,
    val totalDrawsAnalyzed: Int,
    val statisticalScorePercent: Double,
    val exactHitProbabilityPercent: Double,
    val exactHitOddsText: String,
    val atLeastOneHitProbabilityPercent: Double,
    val exactGroupHistoricalHits: Int,
    val exactGroupHitDates: List<String>,
    val partial2PlusHistoricalHits: Int,
    val numberDetails: List<UserNumberDetail>,
    val pairSynergies: List<UserPairSynergy>,
    val feasibilityLevel: String,
    val verdictExplanation: String,
    val recommendations: List<String>
)

data class ChiSquareResult(
    val chiSquareValue: Double,
    val degreesOfFreedom: Int, // 90
    val criticalValue005: Double = 113.145,
    val pValueApprox: Double,
    val isCompatibleWithRandom: Boolean,
    val plainFrenchExplanation: String,
    val totalDrawsAnalyzed: Int = 0,
    val totalObservations: Int = 0,
    val expectedFrequencyPerNumber: Double = 0.0,
    val minObservedFrequency: Int = 0,
    val maxObservedFrequency: Int = 0,
    val hasSufficientData: Boolean = false
)

data class GeneratedCombination(
    val rank: Int,
    val numbers: List<Int>,
    val scorePercent: Double,
    val trueProbabilityText: String = "Ratio théorique combinatoire : 1 sur 46 504 458 (0,0000022 %)",
    val disclaimerText: String = "Statistiques sur les tirages passés, à titre informatif. Elles ne permettent pas de prédire les tirages futurs."
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
     * Calcule toutes les statistiques descriptives pour chaque numéro de 0 à 90.
     */
    fun calculateNumberStats(draws: List<LotoDraw>): List<NumberStat> {
        val totalDraws = draws.size
        if (totalDraws == 0) {
            return (0..90).map {
                NumberStat(it, 0, 0.0, 0, 0, 0)
            }
        }

        val sortedDraws = draws.sortedBy { it.dateString }
        val recentDraws = sortedDraws.takeLast(30)

        val counts = IntArray(TOTAL_NUMBERS)
        val recentCounts = IntArray(TOTAL_NUMBERS)
        val currentGaps = IntArray(TOTAL_NUMBERS) { totalDraws }
        val maxGaps = IntArray(TOTAL_NUMBERS) { 0 }
        val lastSeenIndex = IntArray(TOTAL_NUMBERS) { -1 }

        for (idx in sortedDraws.indices) {
            val draw = sortedDraws[idx]
            for (n in draw.getNumbersList()) {
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
     * Calcule la fréquence observée de chaque numéro de 01 à 90 à partir des tirages stockés.
     */
    fun calculateNumberFrequencies01To90(draws: List<LotoDraw>): List<NumberFrequency> {
        val totalDraws = draws.size
        val counts = IntArray(91)

        for (draw in draws) {
            for (n in draw.getNumbersList()) {
                if (n in 1..90) {
                    counts[n]++
                }
            }
        }

        return (1..90).map { number ->
            val count = counts[number]
            val pct = if (totalDraws > 0) (count.toDouble() / totalDraws) * 100.0 else 0.0
            NumberFrequency(
                number = number,
                formattedNumber = number.toString().padStart(2, '0'),
                count = count,
                frequencyPercent = pct
            )
        }
    }

    /**
     * Calcule le nombre de tirages écoulés depuis la dernière apparition de chaque numéro (retards).
     */
    fun calculateNumberGapsAndLeastRecent(
        draws: List<LotoDraw>,
        recentWindowSize: Int = 30,
        topLimit: Int = 10
    ): GapAndColdAnalysis {
        val safeWindow = recentWindowSize.coerceAtLeast(1)
        val safeLimit = topLimit.coerceIn(1, TOTAL_NUMBERS)
        val totalDraws = draws.size

        if (totalDraws == 0) {
            val emptyGaps = (0..90).map { n ->
                NumberGapStat(
                    number = n,
                    formattedNumber = n.toString().padStart(2, '0'),
                    drawsSinceLastAppearance = 0,
                    maxGap = 0,
                    lastSeenDate = null,
                    recentAppearances = 0,
                    totalAppearances = 0,
                    neverAppeared = true
                )
            }
            return GapAndColdAnalysis(
                totalDrawsAnalyzed = 0,
                recentWindowSize = safeWindow,
                numberGaps = emptyGaps,
                longestGaps = emptyList(),
                leastRecentlyDrawn = emptyList()
            )
        }

        val sortedDraws = draws.sortedBy { it.dateString }
        val effectiveWindow = minOf(safeWindow, totalDraws)
        val recentDraws = sortedDraws.takeLast(effectiveWindow)

        val totalCounts = IntArray(TOTAL_NUMBERS)
        val recentCounts = IntArray(TOTAL_NUMBERS)
        val maxGaps = IntArray(TOTAL_NUMBERS)
        val lastSeenIndex = IntArray(TOTAL_NUMBERS) { -1 }
        val lastSeenDates = arrayOfNulls<String>(TOTAL_NUMBERS)

        for (idx in sortedDraws.indices) {
            val draw = sortedDraws[idx]
            for (n in draw.getNumbersList().distinct()) {
                if (n in 0..90) {
                    totalCounts[n]++
                    val gapSinceLast = if (lastSeenIndex[n] == -1) idx else idx - lastSeenIndex[n] - 1
                    if (gapSinceLast > maxGaps[n]) {
                        maxGaps[n] = gapSinceLast
                    }
                    lastSeenIndex[n] = idx
                    lastSeenDates[n] = draw.dateString
                }
            }
        }

        for (draw in recentDraws) {
            for (n in draw.getNumbersList().distinct()) {
                if (n in 0..90) {
                    recentCounts[n]++
                }
            }
        }

        val numberGaps = (0..90).map { n ->
            val lastIdx = lastSeenIndex[n]
            val neverAppeared = lastIdx == -1
            val currentGap = if (neverAppeared) totalDraws else (totalDraws - 1) - lastIdx
            val maxGap = maxOf(maxGaps[n], currentGap)
            NumberGapStat(
                number = n,
                formattedNumber = n.toString().padStart(2, '0'),
                drawsSinceLastAppearance = currentGap,
                maxGap = maxGap,
                lastSeenDate = lastSeenDates[n],
                recentAppearances = recentCounts[n],
                totalAppearances = totalCounts[n],
                neverAppeared = neverAppeared
            )
        }

        val longestGaps = numberGaps
            .sortedWith(
                compareByDescending<NumberGapStat> { it.drawsSinceLastAppearance }
                    .thenBy { it.totalAppearances }
                    .thenBy { it.number }
            )
            .take(safeLimit)

        val leastRecentlyDrawn = numberGaps
            .sortedWith(
                compareBy<NumberGapStat> { it.recentAppearances }
                    .thenByDescending { it.drawsSinceLastAppearance }
                    .thenBy { it.totalAppearances }
                    .thenBy { it.number }
            )
            .take(safeLimit)

        return GapAndColdAnalysis(
            totalDrawsAnalyzed = totalDraws,
            recentWindowSize = effectiveWindow,
            numberGaps = numberGaps,
            longestGaps = longestGaps,
            leastRecentlyDrawn = leastRecentlyDrawn
        )
    }

    fun calculateTopPairs(draws: List<LotoDraw>, limit: Int = 10): List<PairStat> {
        if (draws.isEmpty() || limit <= 0) return emptyList()
        val pairCounts = mutableMapOf<Pair<Int, Int>, Int>()

        for (draw in draws) {
            val nums = draw.getNumbersList().distinct()
            for (i in 0 until nums.size) {
                for (j in i + 1 until nums.size) {
                    val pair = Pair(nums[i], nums[j])
                    pairCounts[pair] = (pairCounts[pair] ?: 0) + 1
                }
            }
        }

        return pairCounts.entries
            .sortedWith(
                compareByDescending<Map.Entry<Pair<Int, Int>, Int>> { it.value }
                    .thenBy { it.key.first }
                    .thenBy { it.key.second }
            )
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

    fun calculateTopTrios(draws: List<LotoDraw>, limit: Int = 10): List<TrioStat> {
        if (draws.isEmpty() || limit <= 0) return emptyList()
        val trioCounts = mutableMapOf<Triple<Int, Int, Int>, Int>()

        for (draw in draws) {
            val nums = draw.getNumbersList().distinct()
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
            .sortedWith(
                compareByDescending<Map.Entry<Triple<Int, Int, Int>, Int>> { it.value }
                    .thenBy { it.key.first }
                    .thenBy { it.key.second }
                    .thenBy { it.key.third }
            )
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

    fun analyzeTopPairsAndTrios(
        draws: List<LotoDraw>,
        pairLimit: Int = 10,
        trioLimit: Int = 10
    ): PairsAndTriosAnalysis {
        return PairsAndTriosAnalysis(
            totalDrawsAnalyzed = draws.size,
            topPairs = calculateTopPairs(draws, pairLimit),
            topTrios = calculateTopTrios(draws, trioLimit)
        )
    }

    fun calculateChiSquareTest(draws: List<LotoDraw>): ChiSquareResult {
        val totalDraws = draws.size
        if (totalDraws < 10) {
            return ChiSquareResult(
                chiSquareValue = 0.0,
                degreesOfFreedom = TOTAL_NUMBERS - 1,
                pValueApprox = 1.0,
                isCompatibleWithRandom = true,
                plainFrenchExplanation = "Données insuffisantes (minimum 10 tirages requis pour une interprétation statistique fiable).",
                totalDrawsAnalyzed = totalDraws,
                totalObservations = totalDraws * COMBINATION_SIZE,
                expectedFrequencyPerNumber = if (totalDraws > 0) (totalDraws * COMBINATION_SIZE.toDouble()) / TOTAL_NUMBERS else 0.0,
                minObservedFrequency = 0,
                maxObservedFrequency = 0,
                hasSufficientData = false
            )
        }

        val counts = IntArray(TOTAL_NUMBERS)
        for (draw in draws) {
            for (n in draw.getNumbersList().distinct()) {
                if (n in 0..90) counts[n]++
            }
        }

        return calculateChiSquareFromObservedCounts(
            observedCounts = counts,
            totalDrawsAnalyzed = totalDraws
        )
    }

    fun calculateChiSquareFromObservedCounts(
        observedCounts: IntArray,
        totalDrawsAnalyzed: Int = (observedCounts.sum() / COMBINATION_SIZE)
    ): ChiSquareResult {
        val normalized = IntArray(TOTAL_NUMBERS)
        val limit = minOf(observedCounts.size, TOTAL_NUMBERS)
        var totalObservations = 0
        var minObserved = Int.MAX_VALUE
        var maxObserved = 0

        for (i in 0 until TOTAL_NUMBERS) {
            val value = if (i < limit) observedCounts[i].coerceAtLeast(0) else 0
            normalized[i] = value
            totalObservations += value
            if (value < minObserved) minObserved = value
            if (value > maxObserved) maxObserved = value
        }

        val df = TOTAL_NUMBERS - 1 // 90
        if (totalObservations < 10 * COMBINATION_SIZE) {
            return ChiSquareResult(
                chiSquareValue = 0.0,
                degreesOfFreedom = df,
                pValueApprox = 1.0,
                isCompatibleWithRandom = true,
                plainFrenchExplanation = "Données insuffisantes (minimum 10 tirages requis pour une interprétation statistique fiable).",
                totalDrawsAnalyzed = totalDrawsAnalyzed,
                totalObservations = totalObservations,
                expectedFrequencyPerNumber = totalObservations.toDouble() / TOTAL_NUMBERS,
                minObservedFrequency = if (minObserved == Int.MAX_VALUE) 0 else minObserved,
                maxObservedFrequency = maxObserved,
                hasSufficientData = false
            )
        }

        val expected = totalObservations.toDouble() / TOTAL_NUMBERS

        var chi2 = 0.0
        for (observed in normalized) {
            val diff = observed - expected
            chi2 += (diff * diff) / expected
        }

        val pValue = approximateChiSquarePValue(chi2, df)
        val criticalVal = 113.145

        val isRandom = chi2 <= criticalVal
        val roundedChi2 = "%.2f".format(chi2)
        val roundedP = "%.3f".format(pValue)

        val explanation = if (isRandom) {
            "Le test du Khi-carré (χ² = $roundedChi2, p = $roundedP) montre que la répartition observée est compatible avec une distribution uniforme (tirage équiprobable des 91 numéros)."
        } else {
            "Le test du Khi-carré (χ² = $roundedChi2, p = $roundedP) relève un écart de fréquence observée sur cet échantillon passé, sans remettre en cause l'indépendance des tirages futurs."
        }

        return ChiSquareResult(
            chiSquareValue = chi2,
            degreesOfFreedom = df,
            criticalValue005 = criticalVal,
            pValueApprox = pValue,
            isCompatibleWithRandom = isRandom,
            plainFrenchExplanation = explanation,
            totalDrawsAnalyzed = totalDrawsAnalyzed,
            totalObservations = totalObservations,
            expectedFrequencyPerNumber = expected,
            minObservedFrequency = if (minObserved == Int.MAX_VALUE) 0 else minObserved,
            maxObservedFrequency = maxObserved,
            hasSufficientData = true
        )
    }

    private fun approximateChiSquarePValue(chi2: Double, df: Int): Double {
        if (chi2 <= 0.0) return 1.0
        val term = (chi2 / df).pow(1.0 / 3.0)
        val mean = 1.0 - (2.0 / (9.0 * df))
        val stdDev = sqrt(2.0 / (9.0 * df))
        val z = (term - mean) / stdDev
        return approxStandardNormalSurvival(z).coerceIn(0.0001, 0.9999)
    }

    private fun approxStandardNormalSurvival(z: Double): Double {
        if (z < -7.0) return 1.0
        if (z > 7.0) return 0.0
        val t = 1.0 / (1.0 + 0.2316419 * abs(z))
        val d = 0.3989422804014327 * exp(-z * z / 2.0)
        val poly = t * (0.319381530 + t * (-0.356563782 + t * (1.781477937 + t * (-1.821255978 + t * 1.330274429))))
        val cdf = if (z >= 0) 1.0 - d * poly else d * poly
        return 1.0 - cdf
    }

    fun generate5ProposedCombinations(
        stats: List<NumberStat>,
        weightRecent: Float = 40f,
        weightGlobal: Float = 30f,
        weightGap: Float = 30f
    ): List<GeneratedCombination> {
        if (stats.isEmpty()) return emptyList()

        val totalWeight = (weightRecent + weightGlobal + weightGap).coerceAtLeast(1f)
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

        val comb1Numbers = scoredNumbers.take(5).map { it.number }.sorted()
        val comb1Score = scoredNumbers.take(5).map { it.score }.average()
        combinations.add(
            GeneratedCombination(
                rank = 1,
                numbers = comb1Numbers,
                scorePercent = roundOneDecimal(comb1Score)
            )
        )

        val comb2Pool = listOfNotNull(
            scoredNumbers.getOrNull(0),
            scoredNumbers.getOrNull(1),
            scoredNumbers.getOrNull(2),
            scoredNumbers.getOrNull(5),
            scoredNumbers.getOrNull(6)
        )
        if (comb2Pool.size == 5) {
            combinations.add(
                GeneratedCombination(
                    rank = 2,
                    numbers = comb2Pool.map { it.number }.sorted(),
                    scorePercent = roundOneDecimal(comb2Pool.map { it.score }.average())
                )
            )
        }

        val topRecent = scoredNumbers.sortedByDescending { it.recentCount }.take(3)
        val topGaps = scoredNumbers.sortedByDescending { it.currentGap }
            .filter { it.number !in topRecent.map { r -> r.number } }
            .take(2)
        val comb3Pool = topRecent + topGaps
        if (comb3Pool.size == 5) {
            combinations.add(
                GeneratedCombination(
                    rank = 3,
                    numbers = comb3Pool.map { it.number }.sorted(),
                    scorePercent = roundOneDecimal(comb3Pool.map { it.score }.average())
                )
            )
        }

        val comb4Pool = listOf(3, 4, 7, 8, 9).mapNotNull { scoredNumbers.getOrNull(it) }
        if (comb4Pool.size == 5) {
            combinations.add(
                GeneratedCombination(
                    rank = 4,
                    numbers = comb4Pool.map { it.number }.sorted(),
                    scorePercent = roundOneDecimal(comb4Pool.map { it.score }.average())
                )
            )
        }

        val comb5Pool = listOf(1, 4, 7, 10, 12).mapNotNull { scoredNumbers.getOrNull(it) }
        if (comb5Pool.size == 5) {
            combinations.add(
                GeneratedCombination(
                    rank = 5,
                    numbers = comb5Pool.map { it.number }.sorted(),
                    scorePercent = roundOneDecimal(comb5Pool.map { it.score }.average())
                )
            )
        }

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

    fun evaluateTrackingBalance(
        evaluatedCombinations: List<ProposedCombination>
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
                conclusionText = "Aucune sélection enregistrée pour le suivi. Enregistrez une synthèse NAP ou une grille et importez de nouveaux tirages pour observer la comparaison empirique."
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
        val avgExpected = 25.0 / 91.0

        val conclusion = if (abs(avgObserved - avgExpected) < 0.10) {
            "Résultat conforme à une distribution aléatoire : la moyenne observée (${"%.3f".format(avgObserved)}) reste proche de l'espérance théorique (0,275), illustrant l'indépendance des tirages."
        } else if (avgObserved > avgExpected) {
            "Moyenne observée (${"%.3f".format(avgObserved)}) légèrement supérieure à l'espérance théorique (0,275) sur cet échantillon limité ; la variance naturelle fluctue autour de cette valeur."
        } else {
            "Moyenne observée (${"%.3f".format(avgObserved)}) légèrement inférieure à l'espérance théorique (0,275) sur cet échantillon, illustrant la variance naturelle."
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
     * Calcule la synthèse historique groupée par formats NAP 1, NAP 2, NAP 3 et PERM 5.
     */
    fun calculateDailyNapPackage(
        draws: List<LotoDraw>,
        targetDate: String
    ): DailyNapPackage {
        val stats = calculateNumberStats(draws)
        val topPairs = calculateTopPairs(draws, limit = 25)
        val topTrios = calculateTopTrios(draws, limit = 15)

        val scoredBankers = stats.map { item ->
            val recentBonus = item.recentCount * 12.0
            val gapBonus = when (item.currentGap) {
                in 6..16 -> 25.0
                in 17..28 -> 18.0
                in 0..2 -> 8.0
                else -> 12.0
            }
            val globalRate = item.frequencyPercent * 1.5
            Pair(item, recentBonus + gapBonus + globalRate)
        }.sortedByDescending { it.second }

        val bestBanker = scoredBankers.getOrNull(0)?.first?.number ?: 12
        val altBanker1 = scoredBankers.getOrNull(1)?.first?.number ?: 45
        val altBanker2 = scoredBankers.getOrNull(2)?.first?.number ?: 78

        val bankerStat = stats.find { it.number == bestBanker }
        val nap1 = NapItem(
            title = "NAP 1 (N1 - Repère principal)",
            code = "N1",
            numbers = listOf(bestBanker),
            confidencePercent = 88,
            description = "Numéro n°1 en fréquence observée pondérée. Sorti ${bankerStat?.count ?: 0} fois (écart actuel : ${bankerStat?.currentGap ?: 0} tirages).",
            ruleExplanation = "Format à 1 numéro : correspondance validée si ce numéro figure parmi les 5 tirés."
        )

        val nap2List = mutableListOf<NapItem>()

        val pairWithBanker = topPairs.find { it.n1 == bestBanker || it.n2 == bestBanker }
        val pair1Nums = if (pairWithBanker != null) {
            listOf(pairWithBanker.n1, pairWithBanker.n2).sorted()
        } else {
            listOf(bestBanker, altBanker1).sorted()
        }
        nap2List.add(
            NapItem(
                title = "NAP 2 n°1 (Paire associée au repère)",
                code = "N2",
                numbers = pair1Nums,
                confidencePercent = 75,
                description = "Associe le numéro $bestBanker au numéro co-occurrent le plus fréquent dans l'historique.",
                ruleExplanation = "Format à 2 numéros : correspondance complète si les 2 chiffres figurent dans le même tirage."
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
                title = "NAP 2 n°2 (Paire la plus fréquente)",
                code = "N2",
                numbers = pair2Nums,
                confidencePercent = 71,
                description = "Paire observée ensemble le plus grand nombre de fois (${bestGlobalPair?.count ?: 0} sorties communes).",
                ruleExplanation = "Format à 2 numéros : correspondance complète si les 2 chiffres figurent dans le même tirage."
            )
        )

        val topRecentNum = stats.maxByOrNull { it.recentCount }?.number ?: altBanker1
        val topGapNum = stats.filter { it.number != topRecentNum }.maxByOrNull { it.currentGap }?.number ?: altBanker2
        val pair3Nums = listOf(topRecentNum, topGapNum).sorted()
        nap2List.add(
            NapItem(
                title = "NAP 2 n°3 (Paire Fréquence & Écart)",
                code = "N2",
                numbers = pair3Nums,
                confidencePercent = 67,
                description = "Associe le numéro le plus fréquent récemment ($topRecentNum) et le plus grand écart ($topGapNum).",
                ruleExplanation = "Format à 2 numéros : correspondance complète si les 2 chiffres figurent dans le même tirage."
            )
        )

        val nap3List = mutableListOf<NapItem>()

        val bestGlobalTrio = topTrios.firstOrNull()
        val trio1Nums = if (bestGlobalTrio != null) {
            listOf(bestGlobalTrio.n1, bestGlobalTrio.n2, bestGlobalTrio.n3).sorted()
        } else {
            listOf(bestBanker, altBanker1, altBanker2).sorted()
        }
        nap3List.add(
            NapItem(
                title = "NAP 3 n°1 (Trio le plus fréquent)",
                code = "N3",
                numbers = trio1Nums,
                confidencePercent = 60,
                description = "Trio observé ensemble le plus grand nombre de fois (${bestGlobalTrio?.count ?: 0} sorties communes).",
                ruleExplanation = "Format à 3 numéros : correspondance complète si les 3 chiffres figurent dans le même tirage."
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
                title = "NAP 3 n°2 (Trio combiné)",
                code = "N3",
                numbers = trio2Nums,
                confidencePercent = 56,
                description = "Trio regroupant le numéro $bestBanker et deux numéros fréquents de l'historique.",
                ruleExplanation = "Format à 3 numéros : correspondance complète si les 3 chiffres figurent dans le même tirage."
            )
        )

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
            title = "PERM 5 (Synthèse 5 Numéros)",
            code = "PERM5",
            numbers = perm5Nums,
            confidencePercent = 82,
            description = "Synthèse descriptive regroupant 5 numéros issus des fréquences et écarts passés.",
            ruleExplanation = "Format à 5 numéros uniquement à titre d'observation statistique."
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

    /**
     * Analyse descriptive d'une sélection personnelle de 1 à 5 numéros.
     */
    fun analyzeUserProposedNumbers(
        proposedNumbers: List<Int>,
        draws: List<LotoDraw>
    ): UserNumbersAnalysis {
        val cleanNumbers = proposedNumbers.filter { it in 0..90 }.distinct().sorted().take(5)
        require(cleanNumbers.isNotEmpty()) { "Au moins 1 numéro entre 0 et 90 est requis." }

        val k = cleanNumbers.size
        val gameTypeLabel = when (k) {
            1 -> "NAP 1 (N1 - 1 numéro)"
            2 -> "NAP 2 (N2 - 2 numéros)"
            3 -> "NAP 3 (N3 - 3 numéros)"
            4 -> "Sélection 4 numéros"
            else -> "Sélection 5 numéros"
        }

        val totalCombos = binomialLong(TOTAL_NUMBERS, COMBINATION_SIZE).toDouble() // 46 504 458
        val favorableExact = binomialLong(TOTAL_NUMBERS - k, COMBINATION_SIZE - k).toDouble()
        val exactProb = (favorableExact / totalCombos).coerceIn(0.0, 1.0)
        val exactProbPct = exactProb * 100.0
        val oneInX = if (favorableExact > 0.0) totalCombos / favorableExact else totalCombos
        val formattedOdds = when {
            oneInX < 1000.0 -> "%.1f".format(oneInX).replace('.', ',')
            else -> "%,d".format(Math.round(oneInX)).replace(',', ' ')
        }
        val formattedExactPct = when {
            exactProbPct >= 0.1 -> "%.2f".format(exactProbPct).replace('.', ',')
            exactProbPct >= 0.001 -> "%.4f".format(exactProbPct).replace('.', ',')
            else -> "%.7f".format(exactProbPct).replace('.', ',')
        }
        val exactHitOddsText = "1 sur $formattedOdds ($formattedExactPct %)"

        val zeroHitsWays = binomialLong(TOTAL_NUMBERS - k, COMBINATION_SIZE).toDouble()
        val atLeastOneProbPct = ((1.0 - (zeroHitsWays / totalCombos)) * 100.0).coerceIn(0.0, 100.0)

        val stats = calculateNumberStats(draws)
        val statsMap = stats.associateBy { it.number }
        val totalDraws = draws.size

        val maxGlobal = stats.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
        val maxRecent = stats.maxOfOrNull { it.recentCount }?.coerceAtLeast(1) ?: 1
        val maxGap = stats.maxOfOrNull { it.currentGap }?.coerceAtLeast(1) ?: 1

        val numberDetails = cleanNumbers.map { num ->
            val st = statsMap[num] ?: NumberStat(num, 0, 0.0, 0, 0, 0)
            val individualScore = if (totalDraws == 0) {
                50.0
            } else {
                val normRecent = (st.recentCount.toDouble() / maxRecent) * 100.0
                val normGlobal = (st.count.toDouble() / maxGlobal) * 100.0
                val normGap = (st.currentGap.toDouble() / maxGap) * 100.0
                roundOneDecimal((normRecent * 0.40) + (normGlobal * 0.30) + (normGap * 0.30))
            }
            val trendLabel = when {
                totalDraws == 0 -> "Historique vide"
                st.count == 0 -> "Jamais observé"
                st.recentCount >= 3 && st.currentGap <= 6 -> "Fréquent récemment"
                st.currentGap >= 18 -> "Écart élevé (${st.currentGap} tirages)"
                st.recentCount == 0 -> "Peu sorti récemment"
                else -> "Fréquence régulière"
            }
            UserNumberDetail(
                number = num,
                formattedNumber = num.toString().padStart(2, '0'),
                totalCount = st.count,
                frequencyPercent = roundOneDecimal(st.frequencyPercent),
                recentCount = st.recentCount,
                currentGap = st.currentGap,
                maxGap = st.maxGap,
                individualScorePercent = individualScore,
                trendLabel = trendLabel
            )
        }

        val sortedNewestFirst = draws.sortedByDescending { it.dateString }
        val exactHitDates = mutableListOf<String>()
        var partial2PlusHits = 0
        val cleanSet = cleanNumbers.toSet()

        for (draw in sortedNewestFirst) {
            val drawNums = draw.getNumbersList().distinct()
            val matches = drawNums.count { it in cleanSet }
            if (matches == k) {
                exactHitDates.add(draw.dateString)
            }
            if (k >= 2 && matches >= 2) {
                partial2PlusHits++
            } else if (k == 1 && matches == 1) {
                partial2PlusHits++
            }
        }

        val pairSynergies = mutableListOf<UserPairSynergy>()
        if (k >= 2) {
            for (i in 0 until cleanNumbers.size) {
                for (j in i + 1 until cleanNumbers.size) {
                    val a = cleanNumbers[i]
                    val b = cleanNumbers[j]
                    val countTogether = draws.count { draw ->
                        val nums = draw.getNumbersList()
                        nums.contains(a) && nums.contains(b)
                    }
                    val pct = if (totalDraws > 0) roundOneDecimal((countTogether.toDouble() / totalDraws) * 100.0) else 0.0
                    pairSynergies.add(
                        UserPairSynergy(
                            n1 = a,
                            n2 = b,
                            coOccurrenceCount = countTogether,
                            coOccurrencePercent = pct
                        )
                    )
                }
            }
            pairSynergies.sortByDescending { it.coOccurrenceCount }
        }

        val baseScore = numberDetails.map { it.individualScorePercent }.average()
        val synergyBonus = when {
            totalDraws == 0 || k == 1 -> 0.0
            exactHitDates.isNotEmpty() -> 8.0
            pairSynergies.any { it.coOccurrenceCount > 0 } -> 4.0
            else -> -4.0
        }
        val statisticalScore = roundOneDecimal((baseScore + synergyBonus).coerceIn(5.0, 98.0))

        val feasibilityLevel = when (k) {
            1 -> "Indicateur unitaire (NAP 1)"
            2 -> if (exactHitDates.isNotEmpty()) "Paire déjà observée (NAP 2)" else "Paire modérée (NAP 2)"
            3 -> if (exactHitDates.isNotEmpty()) "Trio déjà observé en historique" else "Trio rare en combinaison"
            else -> "Combinaison complète très rare"
        }

        val numbersFormatted = cleanNumbers.joinToString(" - ") { it.toString().padStart(2, '0') }
        val strongest = numberDetails.maxByOrNull { it.individualScorePercent }
        val hottestGap = numberDetails.maxByOrNull { it.currentGap }

        val verdictExplanation = buildString {
            append("Pour votre sélection ($numbersFormatted) en $gameTypeLabel, ")
            append("le ratio combinatoire théorique fixe qu'elle sorte au complet lors d'un tirage aléatoire est de $exactHitOddsText")
            if (k >= 2) {
                append(" (et ${"%.1f".format(atLeastOneProbPct).replace('.', ',')} % en combinatoire pour au moins 1 numéro présent)")
            }
            append(". ")

            if (totalDraws == 0) {
                append("Aucun tirage n'est encore enregistré dans l'historique pour calculer les fréquences observées passées.")
            } else {
                when (k) {
                    1 -> {
                        val d = numberDetails.first()
                        append("Sur $totalDraws tirages passés, le numéro ${d.formattedNumber} présente une fréquence observée de ${d.frequencyPercent} % (${d.totalCount} sorties), avec un écart actuel de ${d.currentGap} tirages.")
                    }
                    else -> {
                        if (exactHitDates.isNotEmpty()) {
                            append("Dans l'historique ($totalDraws tirages), ces $k numéros ont été observés ensemble ${exactHitDates.size} fois (dernière date : ${exactHitDates.first()}). ")
                        } else {
                            append("Dans les $totalDraws tirages archivés, cette sélection de $k numéros n'a pas été observée au complet")
                            if (partial2PlusHits > 0) {
                                append(", bien qu'au moins 2 d'entre eux aient coïncidé $partial2PlusHits fois")
                            }
                            append(". ")
                        }
                        if (strongest != null) {
                            append("Le numéro ${strongest.formattedNumber} affiche l'indice historique le plus élevé (${strongest.individualScorePercent} %).")
                        }
                    }
                }
            }
        }

        val recommendations = mutableListOf<String>()
        recommendations.add(
            "Statistiques sur les tirages passés, à titre informatif : chaque numéro conserve exactement 5 issues sur 91 (5,49 %) à chaque tirage indépendant."
        )
        if (k >= 3 && exactHitDates.isEmpty() && pairSynergies.isNotEmpty()) {
            val bestPair = pairSynergies.first()
            recommendations.add(
                "Co-occurrence passée notable dans votre sélection : la paire ${bestPair.n1.toString().padStart(2, '0')}-${bestPair.n2.toString().padStart(2, '0')} totalise ${bestPair.coOccurrenceCount} sorties communes."
            )
        }
        if (k >= 2 && strongest != null) {
            recommendations.add(
                "Numéro le plus représenté dans votre sélection : ${strongest.formattedNumber} (${strongest.trendLabel.lowercase()}, ${strongest.totalCount} sorties)."
            )
        }
        if (hottestGap != null && hottestGap.currentGap >= 18) {
            recommendations.add(
                "Le numéro ${hottestGap.formattedNumber} présente un écart passé de ${hottestGap.currentGap} tirages sans apparition (observation historique non prédictive)."
            )
        }

        return UserNumbersAnalysis(
            numbers = cleanNumbers,
            gameTypeLabel = gameTypeLabel,
            totalDrawsAnalyzed = totalDraws,
            statisticalScorePercent = statisticalScore,
            exactHitProbabilityPercent = exactProbPct,
            exactHitOddsText = exactHitOddsText,
            atLeastOneHitProbabilityPercent = atLeastOneProbPct,
            exactGroupHistoricalHits = exactHitDates.size,
            exactGroupHitDates = exactHitDates.take(5),
            partial2PlusHistoricalHits = partial2PlusHits,
            numberDetails = numberDetails,
            pairSynergies = pairSynergies,
            feasibilityLevel = feasibilityLevel,
            verdictExplanation = verdictExplanation,
            recommendations = recommendations
        )
    }

    private fun binomialLong(n: Int, k: Int): Long {
        if (k < 0 || k > n) return 0L
        if (k == 0 || k == n) return 1L
        val kk = minOf(k, n - k)
        var result = 1L
        for (i in 1..kk) {
            result = (result * (n - kk + i)) / i
        }
        return result
    }
}
