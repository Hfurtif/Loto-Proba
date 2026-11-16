package com.example.data.api

import android.util.Log
import com.example.data.model.AiEvaluationRecord
import com.example.data.model.LotoDraw
import com.example.stats.NumberStat
import com.example.stats.PairStat
import com.example.stats.UserNumbersAnalysis
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
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiLotoService"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private const val PRIMARY_MODEL = "gemini-2.5-flash"
        private const val FALLBACK_MODEL = "gemini-1.5-flash"
    }

    /**
     * Génère une prédiction complète par IA pour le prochain tirage.
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
            // Génération par algorithme statistique expert si pas de clé API encore configurée
            return@withContext Result.success(
                generateLocalFallbackPrediction(recentDraws, numberStats, topPairs, sourceId, todayStr, targetDateStr)
            )
        }

        try {
            val prompt = buildPredictionPrompt(recentDraws, numberStats, topPairs, sourceName, targetDateStr)
            val jsonResponse = callGeminiApi(apiKey, prompt)

            val record = parsePredictionResponse(
                jsonText = jsonResponse,
                sourceId = sourceId,
                dateGenerated = todayStr,
                targetDate = targetDateStr,
                recentDraws = recentDraws,
                numberStats = numberStats
            )
            Result.success(record)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur Gemini API, utilisation du modèle statistique de secours", e)
            Result.success(
                generateLocalFallbackPrediction(recentDraws, numberStats, topPairs, sourceId, todayStr, targetDateStr)
            )
        }
    }

    /**
     * Analyse rétrospective du dernier tirage sorti par rapport à la prédiction de l'IA :
     * Détermine si l'IA a eu raison ou tort, analyse les causes des numéros sortis,
     * et formule des recommandations sur ce qu'il faut faire pour le prochain tirage.
     */
    suspend fun evaluateDrawAgainstPrediction(
        latestDraw: LotoDraw,
        prediction: AiEvaluationRecord,
        recentDraws: List<LotoDraw>
    ): Result<AiEvaluationRecord> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyManager.getEffectiveApiKey()
        val winningNumbers = latestDraw.getNumbersList()

        // Calcul mathématique des correspondances
        val bankerHit = winningNumbers.contains(prediction.bankerN1)
        val perm5Matches = prediction.getPerm5List().filter { winningNumbers.contains(it) }
        val nap2Hits = prediction.getNap2List().filter { pair -> pair.all { winningNumbers.contains(it) } }
        val nap3Hits = prediction.getNap3List().filter { trio -> trio.all { winningNumbers.contains(it) } }

        if (apiKey.isEmpty() || apiKey.contains("MY_GEMINI_API_KEY", ignoreCase = true)) {
            // Rapport d'évaluation statistique local
            val localEvaluation = generateLocalFallbackEvaluation(
                latestDraw, prediction, bankerHit, perm5Matches, nap2Hits, nap3Hits
            )
            return@withContext Result.success(localEvaluation)
        }

        try {
            val prompt = buildEvaluationPrompt(
                latestDraw = latestDraw,
                prediction = prediction,
                recentDraws = recentDraws,
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
                "Le tirage du ${latestDraw.dateString} a sorti les numéros ${winningNumbers.joinToString(" - ")}."
            }
            val actionAdvice = evaluationJson.optString("actionAdviceNextDraw").ifEmpty {
                "Conserver une pondération prudente et surveiller les numéros en retard d'écart."
            }

            val evaluatedRecord = prediction.copy(
                evaluated = true,
                actualDrawDate = latestDraw.dateString,
                actualWinningNumbersCsv = winningNumbers.joinToString(","),
                verdictSummary = verdictSummary,
                explanationWhy = explanationWhy,
                actionAdviceNextDraw = actionAdvice,
                matchedCount = perm5Matches.size
            )
            Result.success(evaluatedRecord)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur Gemini API pendant l'évaluation, repli local", e)
            val localEvaluation = generateLocalFallbackEvaluation(
                latestDraw, prediction, bankerHit, perm5Matches, nap2Hits, nap3Hits
            )
            Result.success(localEvaluation)
        }
    }

    /**
     * Analyse par l'IA des chiffres proposés par l'utilisateur pour évaluer
     * dans quelle mesure ils ont des chances de sortir au prochain tirage.
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
            Log.e(TAG, "Erreur Gemini API sur analyse des chiffres utilisateur, repli local", e)
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
                        put("temperature", 0.4)
                        put("maxOutputTokens", 1200)
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

    private fun buildPredictionPrompt(
        recentDraws: List<LotoDraw>,
        numberStats: List<NumberStat>,
        topPairs: List<PairStat>,
        sourceName: String,
        targetDate: String
    ): String {
        val last10DrawsStr = recentDraws.take(10).joinToString("\n") {
            "- ${it.dateString} : ${it.getNumbersList().joinToString(" - ")}"
        }
        val topHotStr = numberStats.sortedByDescending { it.count }.take(8).joinToString(", ") { "N°${it.number} (${it.count} sorties)" }
        val topGapsStr = numberStats.sortedByDescending { it.currentGap }.take(8).joinToString(", ") { "N°${it.number} (écart ${it.currentGap})" }
        val topPairsStr = topPairs.take(5).joinToString(", ") { "${it.n1}-${it.n2} (${it.count}x)" }

        return """
            Tu es un expert mathématicien et analyste probabiliste du jeu de loto 5/90 (5 numéros distincts tirés parmi 0 à 90) pour la source : $sourceName.
            Voici les données empiriques réelles :
            Derniers tirages :
            $last10DrawsStr
            
            Numéros les plus fréquents (chauds) : $topHotStr
            Numéros en retard critique (fort écart actuel) : $topGapsStr
            Paires les plus synergiques : $topPairsStr
            
            Génère une sélection experte pour le tirage de demain ($targetDate).
            RÈGLES STRICTES :
            - Tous les numéros doivent être compris strictement entre 0 et 90.
            - Choisis un Banker N1 incontournable (NAP 1).
            - Choisis 2 paires solides NAP 2 (ex: [[banker, part1], [numA, numB]]).
            - Choisis 1 trio solide NAP 3 (ex: [[banker, part1, part2]]).
            - Fournis la grille maîtresse PERM 5 contenant 5 numéros distincts (incluant le Banker).
            - Explique de façon limpide et convaincante ton raisonnement statistique (synergie paires, équilibre retard/fréquence, rupture de série).
            - Rappelle que les tirages restent stochastiques et indépendants.

            RÉPONDS UNIQUEMENT AU FORMAT JSON STRICT SANS MARKDOWN NI EXPLICATIONS EXTÉRIEURES :
            {
              "bankerN1": 17,
              "nap2Pairs": [[17, 53], [29, 82]],
              "nap3Trios": [[17, 53, 29]],
              "perm5": [17, 53, 29, 82, 04],
              "confidenceScore": 88,
              "aiReasoning": "Explication détaillée de la sélection..."
            }
        """.trimIndent()
    }

    private fun buildEvaluationPrompt(
        latestDraw: LotoDraw,
        prediction: AiEvaluationRecord,
        recentDraws: List<LotoDraw>,
        bankerHit: Boolean,
        perm5Matches: List<Int>,
        nap2Hits: List<List<Int>>,
        nap3Hits: List<List<Int>>
    ): String {
        val winningStr = latestDraw.getNumbersList().joinToString(" - ")
        val perm5Str = prediction.getPerm5List().joinToString(" - ")

        return """
            Tu es l'analyste en chef de Loto-Proba (jeu 5/90).
            Le nouveau tirage vient de sortir pour la date du ${latestDraw.dateString} !
            RÉSULTAT OFFICIEL GAGNANT : $winningStr
            
            Voici la prédiction qui avait été formulée par l'IA :
            - Banker N1 proposé : ${prediction.bankerN1} (Résultat : ${if (bankerHit) "SORTI (GAGNÉ)" else "NON SORTI"})
            - Grille PERM 5 proposée : $perm5Str
            - Numéros de la grille trouvés : ${if (perm5Matches.isEmpty()) "Aucun" else perm5Matches.joinToString(", ")} (${perm5Matches.size} sur 5)
            - Paires NAP 2 gagnées : ${nap2Hits.size}
            - Trios NAP 3 gagnés : ${nap3Hits.size}
            
            TÂCHE REQUISE :
            1. VERDICT : Dis franchement si l'IA a eu raison ou tort, avec franchise, clarté et bienveillance.
            2. ANALYSE CRITIQUE : Pourquoi ces numéros précis ($winningStr) sont-ils sortis ? Explique la dynamique du tirage (réveil de numéros en sommeil, répétition d'un numéro d'hier, attachement de paires, etc.).
            3. QUE FAIRE DANS CE CAS POUR LE PROCHAIN TIRAGE ? Donne 3 conseils stratégiques et concrets (ex: faut-il garder le Banker s'il a manqué de peu, jouer la contre-tendance, surveiller les compléments, comment ajuster la mise).

            RÉPONDS UNIQUEMENT AU FORMAT JSON STRICT SUIVANT :
            {
              "verdictSummary": "Texte court du verdict (ex: 🟢 SUCCÈS MAJEUR : Le Banker 17 est sorti ! ou 🟡 BILAN MITIGÉ : 2 numéros captés sur 5...)",
              "explanationWhy": "Analyse approfondie de pourquoi les numéros $winningStr sont tombés...",
              "actionAdviceNextDraw": "Conseils tactiques très concrets : Que faire maintenant pour le prochain tirage..."
            }
        """.trimIndent()
    }

    private fun parsePredictionResponse(
        jsonText: String,
        sourceId: String,
        dateGenerated: String,
        targetDate: String,
        recentDraws: List<LotoDraw>,
        numberStats: List<NumberStat>
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
            "Sélection établie sur la dynamique des sorties récentes et le croisement de l'écart critique avec la fréquence historique."
        }
        val score = json.optInt("confidenceScore", 86)

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

    private fun generateLocalFallbackPrediction(
        recentDraws: List<LotoDraw>,
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
        val part3 = topPairs.firstOrNull { it.n1 == banker || it.n2 == banker }?.let { if (it.n1 == banker) it.n2 else it.n1 } ?: 29
        val part4 = topHot.getOrNull(1) ?: 82
        val part5 = topGaps.getOrNull(1) ?: 4

        val perm5 = listOf(banker, part2, part3, part4, part5).distinct()
        val nap2 = listOf("$banker-$part2", "$banker-$part3")
        val nap3 = listOf("$banker-$part2-$part3")

        val reasoning = "Analyse statistique algorithmique : Le Banker $banker a été retenu pour sa cadence élevée dans les archives. Il est combiné avec le numéro $part2 (en rupture d'écart après un long silence) et la paire historique $part3. (Astuce : configurez votre clé Gemini dans les paramètres pour débloquer l'analyse neuronale complète)."

        return AiEvaluationRecord(
            sourceId = sourceId,
            dateGenerated = todayStr,
            targetDate = targetDateStr,
            bankerN1 = banker,
            nap2PairsCsv = nap2.joinToString(","),
            nap3TriosCsv = nap3.joinToString(","),
            perm5Csv = perm5.take(5).joinToString(","),
            aiReasoning = reasoning,
            confidenceScore = 82,
            evaluated = false
        )
    }

    private fun generateLocalFallbackEvaluation(
        latestDraw: LotoDraw,
        prediction: AiEvaluationRecord,
        bankerHit: Boolean,
        perm5Matches: List<Int>,
        nap2Hits: List<List<Int>>,
        nap3Hits: List<List<Int>>
    ): AiEvaluationRecord {
        val winning = latestDraw.getNumbersList()
        val verdict = when {
            bankerHit && perm5Matches.size >= 2 -> "🟢 SUCCÈS MAJEUR ! Le Banker ${prediction.bankerN1} et ${perm5Matches.size} numéros ont été validés."
            bankerHit -> "🟢 SUCCÈS NAP 1 ! Le Banker ${prediction.bankerN1} est sorti comme prévu."
            perm5Matches.size >= 2 -> "🟡 EXCELLENTE RÉSISTANCE : ${perm5Matches.size} numéros trouvés sur la grille (${perm5Matches.joinToString(", ")})."
            perm5Matches.size == 1 -> "⚪ IMPACT PARTIEL : 1 numéro trouvé (${perm5Matches.first()}). Le tirage a bifurqué vers d'autres séries."
            else -> "🔴 DÉVIATION DU HASARD : 0 numéro concordant sur ce tirage."
        }

        val explanation = "Le tirage réel du ${latestDraw.dateString} a sorti les numéros ${winning.joinToString(" - ")}. " +
                if (bankerHit) {
                    "La stratégie sur le numéro ${prediction.bankerN1} a payé en confirmant la dynamique de fréquence récente."
                } else {
                    "Le tirage s'est orienté vers des numéros inattendus ou des réveils soudains, déjouant les tendances principales de l'historique."
                }

        val advice = if (bankerHit) {
            "Que faire dans ce cas : 1. Encaisser les gains du NAP 1. 2. Pour demain, ne pas rejouer immédiatement le même Banker car les répétitions consécutives sont rares (moins de 5% de chance). 3. Se tourner vers les numéros satellites du tirage d'aujourd'hui."
        } else {
            "Que faire dans ce cas : 1. Ne pas paniquer ni doubler aveuglément les mises : les tirages sont indépendants. 2. Le Banker ${prediction.bankerN1} reste chaud et peut sortir demain par effet d'inertie. 3. Diversifier sur 2 paires espacées pour couvrir la variance."
        }

        return prediction.copy(
            evaluated = true,
            actualDrawDate = latestDraw.dateString,
            actualWinningNumbersCsv = winning.joinToString(","),
            verdictSummary = verdict,
            explanationWhy = explanation,
            actionAdviceNextDraw = advice,
            matchedCount = perm5Matches.size
        )
    }

    private fun buildDefaultVerdict(bankerHit: Boolean, permMatchCount: Int, nap2Count: Int): String {
        return if (bankerHit) {
            "🟢 SUCCÈS DU BANKER (NAP 1 Validé) !"
        } else if (nap2Count > 0) {
            "🟢 PAIRE NAP 2 VALIDÉE ($nap2Count paire gagnante) !"
        } else if (permMatchCount >= 2) {
            "🟡 BIEN VISÉ : $permMatchCount numéros coïncident avec la sélection."
        } else {
            "⚪ Tirage imprévisible : $permMatchCount numéro trouvé."
        }
    }

    private fun buildUserNumbersPrompt(
        analysis: UserNumbersAnalysis,
        recentDraws: List<LotoDraw>,
        sourceName: String
    ): String {
        val chosen = analysis.numbers.joinToString(" - ") { it.toString().padStart(2, '0') }
        val detailsStr = analysis.numberDetails.joinToString("\n") {
            "- Numéro ${it.formattedNumber} : ${it.totalCount} sorties (${it.frequencyPercent}%), ${it.recentCount} récents (30j), retard ${it.currentGap} tirages (max ${it.maxGap}), tendance : ${it.trendLabel}"
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
            Tu es un analyste probabiliste bienveillant et réaliste du loto 5/90 ($sourceName).
            L'utilisateur souhaite jouer SES PROPRES CHIFFRES : $chosen (${analysis.gameTypeLabel}).
            Il te demande : "Dans quelle mesure est-ce possible que ces chiffres viennent ?"

            Données mathématiques et historiques mesurées :
            - Probabilité mathématique exacte du bloc complet : ${analysis.exactHitOddsText}
            - Probabilité qu'au moins 1 chiffre sorte : ${"%.1f".format(analysis.atLeastOneHitProbabilityPercent)} %
            - Score descriptif historique : ${analysis.statisticalScorePercent} %
            - Fois où tous ces chiffres sont sortis ensemble sur ${analysis.totalDrawsAnalyzed} tirages : ${analysis.exactGroupHistoricalHits}
            - Détail par numéro :
            $detailsStr
            - Synergie des paires entre ses numéros : $synergyStr
            - 5 derniers tirages réels :
            $recentStr

            Réponds directement en français simple (4 à 6 phrases maximum, sans JSON ni markdown complexe) :
            1. Dis honnêtement dans quelle mesure c'est possible mathématiquement et historiquement.
            2. Indique quel(s) numéro(s) de sa liste semblent les plus dynamiques ou en retard.
            3. Donne un conseil concret (par exemple garder en NAP 1 / NAP 2 ou ajuster).
        """.trimIndent()
    }

    private fun buildLocalUserNumbersAiSummary(analysis: UserNumbersAnalysis): String {
        val chosen = analysis.numbers.joinToString(" - ") { it.toString().padStart(2, '0') }
        val bestNum = analysis.numberDetails.maxByOrNull { it.individualScorePercent }
        val bestPair = analysis.pairSynergies.maxByOrNull { it.coOccurrenceCount }
        return buildString {
            append("Analyse experte de vos chiffres ($chosen) : ")
            append("Sur le plan mathématique strict, la sortie complète en ${analysis.gameTypeLabel} représente ${analysis.exactHitOddsText}, ")
            append("avec ${"%.1f".format(analysis.atLeastOneHitProbabilityPercent).replace('.', ',')} % de chance qu'au moins un de vos numéros apparaisse. ")
            if (analysis.totalDrawsAnalyzed > 0) {
                if (analysis.exactGroupHistoricalHits > 0) {
                    append("Historiquement, cette combinaison s'est déjà réalisée ${analysis.exactGroupHistoricalHits} fois dans vos archives. ")
                } else {
                    append("Cette sélection exacte ne s'est pas encore produite au complet dans les ${analysis.totalDrawsAnalyzed} tirages analysés. ")
                }
            }
            if (bestNum != null) {
                append("Votre point d'appui principal est le numéro ${bestNum.formattedNumber} (${bestNum.trendLabel.lowercase()}, retard de ${bestNum.currentGap} tirages). ")
            }
            if (bestPair != null && bestPair.coOccurrenceCount > 0) {
                append("La paire ${bestPair.n1.toString().padStart(2, '0')}-${bestPair.n2.toString().padStart(2, '0')} est votre duo le plus solide (${bestPair.coOccurrenceCount} sorties communes).")
            }
        }
    }
}
