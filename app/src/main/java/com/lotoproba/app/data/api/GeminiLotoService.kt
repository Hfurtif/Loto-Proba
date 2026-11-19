package com.lotoproba.app.data.api

import android.util.Log
import com.lotoproba.app.data.model.AiEvaluationRecord
import com.lotoproba.app.data.model.LotoDraw
import com.lotoproba.app.stats.NumberStat
import com.lotoproba.app.stats.PairStat
import com.lotoproba.app.stats.UserNumbersAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GeminiLotoService(private val apiKeyManager: ApiKeyManager) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiLotoService"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private const val PRIMARY_MODEL = "gemini-2.5-flash"
        private const val FALLBACK_MODEL = "gemini-1.5-flash"
    }

    /**
     * Génère une synthèse statistique descriptive sur les données historiques (et non une divination).
     */
    suspend fun generatePrediction(
        recentDraws: List<LotoDraw>,
        numberStats: List<NumberStat>,
        topPairs: List<PairStat>,
        sourceId: String,
        sourceName: String
    ): Result<AiEvaluationRecord> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyManager.getEffectiveApiKey()
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdfDate.format(Date())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val targetDateStr = sdfDate.format(cal.time)

        if (apiKey.isEmpty() || apiKey.contains("MY_GEMINI_API_KEY", ignoreCase = true)) {
            return@withContext Result.success(
                generateLocalFallbackSummary(numberStats, topPairs, sourceId, todayStr, targetDateStr)
            )
        }

        try {
            val prompt = buildSummaryPrompt(recentDraws, numberStats, topPairs, sourceName, targetDateStr)
            val jsonResponse = callGeminiApi(apiKey, prompt)

            val record = parseSummaryResponse(
                jsonText = jsonResponse,
                sourceId = sourceId,
                dateGenerated = todayStr,
                targetDate = targetDateStr
            )
            Result.success(record)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur API Gemini, utilisation de la synthèse locale", e)
            Result.success(
                generateLocalFallbackSummary(numberStats, topPairs, sourceId, todayStr, targetDateStr)
            )
        }
    }

    /**
     * Compare rétrospectivement le dernier tirage enregistré avec la sélection statistique archivée.
     */
    suspend fun evaluateDrawAgainstPrediction(
        latestDraw: LotoDraw,
        prediction: AiEvaluationRecord,
        recentDraws: List<LotoDraw>
    ): Result<AiEvaluationRecord> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyManager.getEffectiveApiKey()
        val drawnNumbers = latestDraw.getNumbersList()

        val bankerHit = drawnNumbers.contains(prediction.bankerN1)
        val perm5Matches = prediction.getPerm5List().filter { drawnNumbers.contains(it) }
        val nap2Hits = prediction.getNap2List().filter { pair -> pair.all { drawnNumbers.contains(it) } }
        val nap3Hits = prediction.getNap3List().filter { trio -> trio.all { drawnNumbers.contains(it) } }

        if (apiKey.isEmpty() || apiKey.contains("MY_GEMINI_API_KEY", ignoreCase = true)) {
            val localEvaluation = generateLocalFallbackEvaluation(
                latestDraw, prediction, bankerHit, perm5Matches
            )
            return@withContext Result.success(localEvaluation)
        }

        try {
            val prompt = buildEvaluationPrompt(
                latestDraw = latestDraw,
                prediction = prediction,
                bankerHit = bankerHit,
                perm5Matches = perm5Matches,
                nap2Hits = nap2Hits,
                nap3Hits = nap3Hits
            )
            val responseText = callGeminiApi(apiKey, prompt)

            val evaluationJson = extractJsonFromResponse(responseText)
            val verdictSummary = evaluationJson.optString("verdictSummary").ifEmpty {
                buildDefaultVerdict(bankerHit, perm5Matches.size, nap2Hits.size)
            }
            val explanationWhy = evaluationJson.optString("explanationWhy").ifEmpty {
                "Le tirage du ${latestDraw.dateString} contient les numéros ${drawnNumbers.joinToString(" - ")}."
            }
            val actionAdvice = evaluationJson.optString("actionAdviceNextDraw").ifEmpty {
                "Ces comparaisons illustrent la variance normale des tirages aléatoires indépendants."
            }

            val evaluatedRecord = prediction.copy(
                evaluated = true,
                actualDrawDate = latestDraw.dateString,
                actualWinningNumbersCsv = drawnNumbers.joinToString(","),
                verdictSummary = verdictSummary,
                explanationWhy = explanationWhy,
                actionAdviceNextDraw = actionAdvice,
                matchedCount = perm5Matches.size
            )
            Result.success(evaluatedRecord)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur API Gemini pendant la comparaison, repli local", e)
            val localEvaluation = generateLocalFallbackEvaluation(
                latestDraw, prediction, bankerHit, perm5Matches
            )
            Result.success(localEvaluation)
        }
    }

    /**
     * Analyse descriptive par IA des numéros sélectionnés par l'utilisateur.
     */
    suspend fun analyzeUserNumbers(
        analysis: UserNumbersAnalysis,
        recentDraws: List<LotoDraw>,
        sourceName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyManager.getEffectiveApiKey()
        if (apiKey.isEmpty() || apiKey.contains("MY_GEMINI_API_KEY", ignoreCase = true)) {
            return@withContext Result.success(buildLocalUserNumbersAiSummary(analysis))
        }

        try {
            val prompt = buildUserNumbersPrompt(analysis, recentDraws, sourceName)
            val responseText = callGeminiApi(apiKey, prompt).trim()
            if (responseText.isNotEmpty()) {
                Result.success(responseText)
            } else {
                Result.success(buildLocalUserNumbersAiSummary(analysis))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur API Gemini sur analyse utilisateur, repli local", e)
            Result.success(buildLocalUserNumbersAiSummary(analysis))
        }
    }

    private fun callGeminiApi(apiKey: String, prompt: String): String {
        val modelsToTry = listOf(PRIMARY_MODEL, FALLBACK_MODEL)
        var lastException: Exception? = null

        for (model in modelsToTry) {
            try {
                val url = "$BASE_URL/$model:generateContent?key=$apiKey"
                val requestJson = JSONObject().apply {
                    val contents = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val parts = JSONArray().apply {
                                put(JSONObject().apply { put("text", prompt) })
                            }
                            put("parts", parts)
                        }
                        put(contentObj)
                    }
                    put("contents", contents)
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.3)
                        put("maxOutputTokens", 1000)
                    })
                }

                val body = requestJson.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url(url).post(body).build()

                client.newCall(request).execute().use { response ->
                    val respBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw IllegalStateException("API error ${response.code}: $respBody")
                    }

                    val root = JSONObject(respBody)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return parts.getJSONObject(0).optString("text")
                        }
                    }
                    throw IllegalStateException("Format de réponse inattendu : $respBody")
                }
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Échec sur $model, tentative avec le modèle suivant...", e)
            }
        }
        throw lastException ?: IllegalStateException("Impossible de joindre l'API Gemini")
    }

    private fun buildSummaryPrompt(
        recentDraws: List<LotoDraw>,
        numberStats: List<NumberStat>,
        topPairs: List<PairStat>,
        sourceName: String,
        targetDate: String
    ): String {
        val last10DrawsStr = recentDraws.take(10).joinToString("\n") {
            "- ${it.dateString} : ${it.getNumbersList().joinToString(" - ")}"
        }
        val topHotStr = numberStats.sortedByDescending { it.count }.take(8)
            .joinToString(", ") { "N°${it.number} (${it.count} sorties)" }
        val topGapsStr = numberStats.sortedByDescending { it.currentGap }.take(8)
            .joinToString(", ") { "N°${it.number} (écart ${it.currentGap})" }
        val topPairsStr = topPairs.take(5).joinToString(", ") { "${it.n1}-${it.n2} (${it.count}x)" }

        return """
            Tu es un statisticien descriptif pour l'application LotoProba (tirages 5/90 : 5 numéros distincts de 0 à 90) pour la source : $sourceName.
            Voici les données observées dans le passé :
            Derniers tirages :
            $last10DrawsStr

            Numéros les plus fréquents historiquement : $topHotStr
            Numéros ayant les plus grands écarts passés : $topGapsStr
            Paires les plus fréquemment observées ensemble : $topPairsStr

            Rédige une synthèse purement descriptive datée du $targetDate.
            RÈGLES STRICTES :
            - Tous les numéros doivent être compris strictement entre 0 et 90.
            - Choisis 1 repère NAP 1 (bankerN1) parmi les plus fréquents/équilibrés.
            - Choisis 2 paires historiques NAP 2.
            - Choisis 1 trio historique NAP 3.
            - Fournis une sélection descriptive de 5 numéros distincts (perm5).
            - Explique clairement qu'il s'agit uniquement d'observations sur le passé, sans aucune capacité à anticiper les tirages à venir.

            RÉPONDS UNIQUEMENT AU FORMAT JSON STRICT SANS MARKDOWN :
            {
              "bankerN1": 17,
              "nap2Pairs": [[17, 53], [29, 82]],
              "nap3Trios": [[17, 53, 29]],
              "perm5": [17, 53, 29, 82, 4],
              "confidenceScore": 50,
              "aiReasoning": "Synthèse descriptive des fréquences observées..."
            }
        """.trimIndent()
    }

    private fun buildEvaluationPrompt(
        latestDraw: LotoDraw,
        prediction: AiEvaluationRecord,
        bankerHit: Boolean,
        perm5Matches: List<Int>,
        nap2Hits: List<List<Int>>,
        nap3Hits: List<List<Int>>
    ): String {
        val drawnStr = latestDraw.getNumbersList().joinToString(" - ")
        val perm5Str = prediction.getPerm5List().joinToString(" - ")

        return """
            Tu es un analyste statistique objectif (jeu 5/90).
            Comparaison rétrospective avec le tirage du ${latestDraw.dateString} : $drawnStr.

            Sélection statistique archivée :
            - Repère NAP 1 : ${prediction.bankerN1} (${if (bankerHit) "présent" else "absent"})
            - Sélection 5 numéros : $perm5Str
            - Correspondances trouvées : ${if (perm5Matches.isEmpty()) "Aucune" else perm5Matches.joinToString(", ")} (${perm5Matches.size}/5)
            - Paires NAP 2 trouvées : ${nap2Hits.size}
            - Trios NAP 3 trouvés : ${nap3Hits.size}

            Fournis une lecture factuelle et pédagogique rappelant que les tirages sont aléatoires et indépendants.
            RÉPONDS UNIQUEMENT AU FORMAT JSON STRICT :
            {
              "verdictSummary": "Résumé factuel des correspondances observées...",
              "explanationWhy": "Commentaire descriptif comparant la sélection archivée aux numéros du tirage...",
              "actionAdviceNextDraw": "Rappel mathématique sur l'indépendance des tirages..."
            }
        """.trimIndent()
    }

    private fun parseSummaryResponse(
        jsonText: String,
        sourceId: String,
        dateGenerated: String,
        targetDate: String
    ): AiEvaluationRecord {
        val json = extractJsonFromResponse(jsonText)
        val banker = json.optInt("bankerN1", 17).coerceIn(0, 90)

        val pairsArr = json.optJSONArray("nap2Pairs")
        val pairsList = mutableListOf<String>()
        if (pairsArr != null) {
            for (i in 0 until pairsArr.length()) {
                val pair = pairsArr.optJSONArray(i)
                if (pair != null && pair.length() >= 2) {
                    val p1 = pair.optInt(0).coerceIn(0, 90)
                    val p2 = pair.optInt(1).coerceIn(0, 90)
                    pairsList.add("$p1-$p2")
                }
            }
        }
        if (pairsList.isEmpty()) pairsList.add("$banker-53")

        val triosArr = json.optJSONArray("nap3Trios")
        val triosList = mutableListOf<String>()
        if (triosArr != null) {
            for (i in 0 until triosArr.length()) {
                val trio = triosArr.optJSONArray(i)
                if (trio != null && trio.length() >= 3) {
                    val t1 = trio.optInt(0).coerceIn(0, 90)
                    val t2 = trio.optInt(1).coerceIn(0, 90)
                    val t3 = trio.optInt(2).coerceIn(0, 90)
                    triosList.add("$t1-$t2-$t3")
                }
            }
        }
        if (triosList.isEmpty()) triosList.add("$banker-53-29")

        val permArr = json.optJSONArray("perm5")
        val permList = mutableListOf<Int>()
        if (permArr != null) {
            for (i in 0 until permArr.length()) {
                val num = permArr.optInt(i).coerceIn(0, 90)
                if (!permList.contains(num)) permList.add(num)
            }
        }
        while (permList.size < 5) {
            val candidate = (0..90).random()
            if (!permList.contains(candidate)) permList.add(candidate)
        }

        val reasoning = json.optString("aiReasoning").ifEmpty {
            "Synthèse descriptive établie sur les fréquences observées et les écarts passés. Ces chiffres n'ont aucune valeur prédictive."
        }
        val score = json.optInt("confidenceScore", 50)

        return AiEvaluationRecord(
            sourceId = sourceId,
            dateGenerated = dateGenerated,
            targetDate = targetDate,
            bankerN1 = banker,
            nap2PairsCsv = pairsList.joinToString(","),
            nap3TriosCsv = triosList.joinToString(","),
            perm5Csv = permList.take(5).joinToString(","),
            aiReasoning = reasoning,
            confidenceScore = score,
            evaluated = false
        )
    }

    private fun extractJsonFromResponse(raw: String): JSONObject {
        val trimmed = raw.trim()
        val startIndex = trimmed.indexOf('{')
        val endIndex = trimmed.lastIndexOf('}')
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            val jsonSub = trimmed.substring(startIndex, endIndex + 1)
            return JSONObject(jsonSub)
        }
        return JSONObject()
    }

    private fun generateLocalFallbackSummary(
        numberStats: List<NumberStat>,
        topPairs: List<PairStat>,
        sourceId: String,
        todayStr: String,
        targetDateStr: String
    ): AiEvaluationRecord {
        val topHot = numberStats.sortedByDescending { it.count }.take(10).map { it.number }
        val topGaps = numberStats.sortedByDescending { it.currentGap }.take(10).map { it.number }

        val banker = topHot.firstOrNull() ?: 17
        val part2 = topGaps.firstOrNull { it != banker } ?: 53
        val part3 = topPairs.firstOrNull { it.n1 == banker || it.n2 == banker }
            ?.let { if (it.n1 == banker) it.n2 else it.n1 } ?: 29
        val part4 = topHot.getOrNull(1) ?: 82
        val part5 = topGaps.getOrNull(1) ?: 4

        val perm5 = listOf(banker, part2, part3, part4, part5).distinct()
        val nap2 = listOf("$banker-$part2", "$banker-$part3")
        val nap3 = listOf("$banker-$part2-$part3")

        val reasoning = "Synthèse statistique locale (hors ligne) : le numéro $banker présente la fréquence observée la plus élevée sur l'historique archivé, accompagné du numéro $part2 (écart actuel le plus long) et du numéro $part3 (co-occurrence observée). Statistiques sur les tirages passés uniquement, à titre informatif."

        return AiEvaluationRecord(
            sourceId = sourceId,
            dateGenerated = todayStr,
            targetDate = targetDateStr,
            bankerN1 = banker,
            nap2PairsCsv = nap2.joinToString(","),
            nap3TriosCsv = nap3.joinToString(","),
            perm5Csv = perm5.take(5).joinToString(","),
            aiReasoning = reasoning,
            confidenceScore = 50,
            evaluated = false
        )
    }

    private fun generateLocalFallbackEvaluation(
        latestDraw: LotoDraw,
        prediction: AiEvaluationRecord,
        bankerHit: Boolean,
        perm5Matches: List<Int>
    ): AiEvaluationRecord {
        val drawn = latestDraw.getNumbersList()
        val verdict = when {
            bankerHit && perm5Matches.size >= 2 ->
                "Correspondance partielle : repère ${prediction.bankerN1} présent et ${perm5Matches.size}/5 numéros retrouvés."
            bankerHit ->
                "Repère NAP 1 (${prediction.bankerN1}) présent parmi les 5 numéros du tirage."
            perm5Matches.size >= 2 ->
                "${perm5Matches.size}/5 numéros de la sélection figurent dans ce tirage (${perm5Matches.joinToString(", ")})."
            perm5Matches.size == 1 ->
                "1 numéro commun observé (${perm5Matches.first()})."
            else ->
                "Aucune correspondance observée sur ce tirage (0/5)."
        }

        val explanation = "Le tirage du ${latestDraw.dateString} a donné les numéros ${drawn.joinToString(" - ")}. " +
            "La comparaison rétrospective montre ${perm5Matches.size} correspondance(s) sur 5, ce qui correspond aux fluctuations normales d'un tirage aléatoire."

        val advice = "Rappel statistique : chaque tirage est indépendant des tirages passés. Les fréquences observées dans l'historique ne modifient jamais l'équiprobabilité des tirages suivants."

        return prediction.copy(
            evaluated = true,
            actualDrawDate = latestDraw.dateString,
            actualWinningNumbersCsv = drawn.joinToString(","),
            verdictSummary = verdict,
            explanationWhy = explanation,
            actionAdviceNextDraw = advice,
            matchedCount = perm5Matches.size
        )
    }

    private fun buildDefaultVerdict(bankerHit: Boolean, permMatchCount: Int, nap2Count: Int): String {
        return when {
            bankerHit -> "Repère NAP 1 observé dans le tirage ($permMatchCount/5 correspondances)."
            nap2Count > 0 -> "$nap2Count paire(s) NAP 2 observée(s) dans le tirage."
            permMatchCount > 0 -> "$permMatchCount/5 correspondance(s) observée(s)."
            else -> "Aucune correspondance observée sur ce tirage (0/5)."
        }
    }

    private fun buildUserNumbersPrompt(
        analysis: UserNumbersAnalysis,
        recentDraws: List<LotoDraw>,
        sourceName: String
    ): String {
        val chosen = analysis.numbers.joinToString(" - ") { it.toString().padStart(2, '0') }
        val detailsStr = analysis.numberDetails.joinToString("\n") {
            "- Numéro ${it.formattedNumber} : ${it.totalCount} sorties (fréquence observée ${it.frequencyPercent} %), ${it.recentCount} récents (30j), écart ${it.currentGap} tirages (max ${it.maxGap}), profil : ${it.trendLabel}"
        }
        val synergyStr = if (analysis.pairSynergies.isEmpty()) {
            "Aucune (1 seul numéro sélectionné)"
        } else {
            analysis.pairSynergies.joinToString(", ") { "${it.n1}-${it.n2}: ${it.coOccurrenceCount}x" }
        }
        val recentStr = recentDraws.take(5).joinToString("\n") {
            "- ${it.dateString} : ${it.getNumbersList().joinToString(" - ")}"
        }

        return """
            Tu es un statisticien descriptif objectif du jeu 5/90 ($sourceName).
            L'utilisateur consulte l'historique pour sa sélection personnelle : $chosen (${analysis.gameTypeLabel}).

            Données mesurées :
            - Ratio combinatoire théorique fixe : ${analysis.exactHitOddsText}
            - Ratio théorique d'au moins 1 chiffre : ${"%.1f".format(analysis.atLeastOneHitProbabilityPercent)} %
            - Indice descriptif historique : ${analysis.statisticalScorePercent} %
            - Apparitions complètes dans les ${analysis.totalDrawsAnalyzed} tirages passés : ${analysis.exactGroupHistoricalHits}
            - Détail par numéro :
            $detailsStr
            - Co-occurrences passées des paires : $synergyStr
            - 5 derniers tirages enregistrés :
            $recentStr

            Réponds en français simple (4 phrases max, sans JSON) en rappelant que les statistiques passées ne permettent pas d'anticiper les tirages à venir.
        """.trimIndent()
    }

    private fun buildLocalUserNumbersAiSummary(analysis: UserNumbersAnalysis): String {
        val chosen = analysis.numbers.joinToString(" - ") { it.toString().padStart(2, '0') }
        val bestNum = analysis.numberDetails.maxByOrNull { it.individualScorePercent }
        val bestPair = analysis.pairSynergies.maxByOrNull { it.coOccurrenceCount }
        return buildString {
            append("Synthèse descriptive de votre sélection ($chosen) : ")
            append("Sur le plan combinatoire théorique (${analysis.gameTypeLabel}), le ratio mathématique fixe est de ${analysis.exactHitOddsText}. ")
            if (analysis.totalDrawsAnalyzed > 0) {
                if (analysis.exactGroupHistoricalHits > 0) {
                    append("Dans les tirages passés (${analysis.totalDrawsAnalyzed} archivés), ce groupe complet a été observé ${analysis.exactGroupHistoricalHits} fois. ")
                } else {
                    append("Ce groupe exact n'a pas été observé au complet dans les ${analysis.totalDrawsAnalyzed} tirages archivés. ")
                }
            }
            if (bestNum != null) {
                append("Le numéro ${bestNum.formattedNumber} présente une fréquence observée de ${bestNum.frequencyPercent} % (écart actuel de ${bestNum.currentGap} tirages). ")
            }
            if (bestPair != null && bestPair.coOccurrenceCount > 0) {
                append("La paire ${bestPair.n1.toString().padStart(2, '0')}-${bestPair.n2.toString().padStart(2, '0')} totalise ${bestPair.coOccurrenceCount} co-occurrences passées. ")
            }
            append("Statistiques à titre informatif uniquement : chaque tirage reste indépendant.")
        }
    }
}
