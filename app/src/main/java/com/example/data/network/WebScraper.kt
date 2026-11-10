package com.example.data.network

import com.example.data.model.LotoDraw
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class WebScraper(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {

    private val frenchMonths = mapOf(
        "janvier" to "01",
        "février" to "02",
        "fevrier" to "02",
        "mars" to "03",
        "avril" to "04",
        "mai" to "05",
        "juin" to "06",
        "juillet" to "07",
        "août" to "08",
        "aout" to "08",
        "septembre" to "09",
        "octobre" to "10",
        "novembre" to "11",
        "décembre" to "12",
        "decembre" to "12"
    )

    /**
     * Télécharge le contenu HTML ou texte de la page web distante
     */
    suspend fun fetchUrl(url: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else {
                url
            }

            val request = Request.Builder()
                .url(formattedUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,text/plain,*/*;q=0.8")
                .header("Accept-Language", "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("Erreur HTTP ${response.code} : ${response.message}")
                    )
                }
                val body = response.body?.string() ?: ""
                Result.success(body)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Analyse et extrait tous les tirages disponibles depuis la page web ou un texte collé.
     * Supporte à la fois :
     * 1. Google Sites / formats paragraphes texte (ex: 'Samedi 07 novembre 2026 // 53 - 59 - 17 - 29 - 51')
     * 2. Tableaux HTML classiques (<tr><td>)
     * 3. Formats CSV et lignes de texte brutes.
     */
    fun parseDraws(
        rawContent: String,
        sourceId: String,
        parserPreset: String = "AUTO"
    ): ImportReport {
        val validDraws = mutableListOf<LotoDraw>()
        val invalidLines = mutableListOf<InvalidLineReport>()
        val seenDates = mutableSetOf<String>()

        // Passerelle A : Extraction optimisée pour les pages de type Google Sites / Texte continu
        val extractedFromTextPattern = extractDrawsFromFormattedText(rawContent, sourceId)
        if (extractedFromTextPattern.isNotEmpty()) {
            var itemIdx = 0
            for (candidate in extractedFromTextPattern) {
                itemIdx++
                if (candidate.draw.isValid()) {
                    if (candidate.draw.dateString !in seenDates) {
                        seenDates.add(candidate.draw.dateString)
                        validDraws.add(candidate.draw)
                    }
                } else {
                    invalidLines.add(
                        InvalidLineReport(
                            lineNumber = itemIdx,
                            rawText = candidate.rawText,
                            reason = candidate.invalidReason ?: "Format non conforme aux règles [0..90]"
                        )
                    )
                }
            }

            return ImportReport(
                totalProcessed = extractedFromTextPattern.size,
                validCount = validDraws.size,
                duplicateCount = 0,
                invalidCount = invalidLines.size,
                invalidLines = invalidLines,
                parsedDraws = validDraws
            )
        }

        // Passerelle B : Extraction ligne par ligne (CSV, Tableaux HTML classiques, etc.)
        val textLines = if (rawContent.contains("<table", ignoreCase = true) || rawContent.contains("<tr", ignoreCase = true)) {
            extractLinesFromHtml(rawContent)
        } else {
            rawContent.lines()
        }

        var lineIndex = 0
        for (line in textLines) {
            lineIndex++
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                continue
            }
            if (trimmed.contains("date", ignoreCase = true) && trimmed.contains("num", ignoreCase = true)) {
                continue
            }

            val parseResult = parseSingleLine(trimmed, sourceId, lineIndex)
            when (parseResult) {
                is LineParseResult.Success -> {
                    if (parseResult.draw.dateString !in seenDates) {
                        seenDates.add(parseResult.draw.dateString)
                        validDraws.add(parseResult.draw)
                    }
                }
                is LineParseResult.Invalid -> {
                    invalidLines.add(parseResult.report)
                }
                is LineParseResult.Ignored -> {}
            }
        }

        return ImportReport(
            totalProcessed = lineIndex,
            validCount = validDraws.size,
            duplicateCount = 0,
            invalidCount = invalidLines.size,
            invalidLines = invalidLines,
            parsedDraws = validDraws
        )
    }

    private data class CandidateDraw(
        val draw: LotoDraw,
        val rawText: String,
        val invalidReason: String? = null
    )

    /**
     * Recherche les séquences Date (en français ou numérique) suivies de 5 numéros
     * Idéal pour sites web structurés comme Google Sites (Loto Bonheur Niger).
     */
    private fun extractDrawsFromFormattedText(html: String, sourceId: String): List<CandidateDraw> {
        val cleanText = html.replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace("&#39;", "'")

        // Regex pour trouver une date en français (ex: "Samedi 07 novembre 2026", "11 mai 2026")
        val frenchDateRegex = Pattern.compile(
            "(?:Lundi|Mardi|Mercredi|Jeudi|Vendredi|Samedi|Dimanche)?\\s*(\\d{1,2})\\s+([a-zA-Zéûàôïê]+)\\s+(\\d{4})",
            Pattern.CASE_INSENSITIVE
        )

        val numberSequenceRegex = Pattern.compile(
            "(\\d{1,3})\\s*[-;, /]\\s*(\\d{1,3})\\s*[-;, /]\\s*(\\d{1,3})\\s*[-;, /]\\s*(\\d{1,3})\\s*[-;, /]\\s*(\\d{1,3})"
        )

        val dateMatches = mutableListOf<Triple<Int, Int, String>>() // start, end, dateString
        val matcher = frenchDateRegex.matcher(cleanText)
        while (matcher.find()) {
            val day = matcher.group(1) ?: continue
            val monthName = (matcher.group(2) ?: "").lowercase()
            val year = matcher.group(3) ?: continue

            val monthNumber = frenchMonths[monthName]
            if (monthNumber != null) {
                val formattedDate = "$year-$monthNumber-${day.padStart(2, '0')}"
                dateMatches.add(Triple(matcher.start(), matcher.end(), formattedDate))
            }
        }

        if (dateMatches.isEmpty()) return emptyList()

        val results = mutableListOf<CandidateDraw>()
        for (i in 0 until dateMatches.size) {
            val current = dateMatches[i]
            val dateStr = current.third
            val startPos = current.second
            val endPos = if (i + 1 < dateMatches.size) dateMatches[i + 1].first else (startPos + 400).coerceAtMost(cleanText.length)

            if (startPos >= cleanText.length) continue
            val subText = cleanText.substring(startPos, endPos.coerceAtMost(cleanText.length))

            val numMatcher = numberSequenceRegex.matcher(subText)
            if (numMatcher.find()) {
                val n1 = numMatcher.group(1)?.toIntOrNull() ?: 0
                val n2 = numMatcher.group(2)?.toIntOrNull() ?: 0
                val n3 = numMatcher.group(3)?.toIntOrNull() ?: 0
                val n4 = numMatcher.group(4)?.toIntOrNull() ?: 0
                val n5 = numMatcher.group(5)?.toIntOrNull() ?: 0

                val nums = listOf(n1, n2, n3, n4, n5)
                val rawSnippet = "$dateStr : $n1-$n2-$n3-$n4-$n5"

                val invalidReason = when {
                    nums.any { it !in 0..90 } -> "Numéro(s) hors bornes [0..90] : ${nums.filter { it !in 0..90 }.joinToString()}"
                    nums.toSet().size != 5 -> "Doublon détecté parmi les numéros : ${nums.joinToString()}"
                    else -> null
                }

                results.add(
                    CandidateDraw(
                        draw = LotoDraw(
                            sourceId = sourceId,
                            dateString = dateStr,
                            drawName = "Tirage Officiel",
                            n1 = n1, n2 = n2, n3 = n3, n4 = n4, n5 = n5
                        ),
                        rawText = rawSnippet,
                        invalidReason = invalidReason
                    )
                )
            }
        }

        return results
    }

    private fun extractLinesFromHtml(html: String): List<String> {
        val lines = mutableListOf<String>()
        val trPattern = Pattern.compile("(?is)<tr[^>]*>(.*?)</tr>")
        val tdPattern = Pattern.compile("(?is)<t[dh][^>]*>(.*?)</t[dh]>")
        val tagRemover = Pattern.compile("<[^>]+>")

        val trMatcher = trPattern.matcher(html)
        while (trMatcher.find()) {
            val trContent = trMatcher.group(1) ?: continue
            val tdMatcher = tdPattern.matcher(trContent)
            val cells = mutableListOf<String>()
            while (tdMatcher.find()) {
                val tdContent = tdMatcher.group(1) ?: ""
                val cleanText = tagRemover.matcher(tdContent).replaceAll(" ").trim()
                if (cleanText.isNotBlank()) {
                    cells.add(cleanText)
                }
            }
            if (cells.isNotEmpty()) {
                lines.add(cells.joinToString(" ; "))
            }
        }

        if (lines.isEmpty()) {
            val stripped = tagRemover.matcher(html).replaceAll("\n")
            return stripped.lines()
        }
        return lines
    }

    private sealed class LineParseResult {
        data class Success(val draw: LotoDraw) : LineParseResult()
        data class Invalid(val report: InvalidLineReport) : LineParseResult()
        object Ignored : LineParseResult()
    }

    private fun parseSingleLine(
        rawLine: String,
        sourceId: String,
        lineNumber: Int
    ): LineParseResult {
        val dateRegex = Pattern.compile("(\\b\\d{4}[-/.]\\d{1,2}[-/.]\\d{1,2}\\b|\\b\\d{1,2}[-/.]\\d{1,2}[-/.]\\d{2,4}\\b)")
        val dateMatcher = dateRegex.matcher(rawLine)

        var dateString: String? = null
        var remainingText = rawLine

        if (dateMatcher.find()) {
            val rawDate = dateMatcher.group(1)
            dateString = normalizeDate(rawDate)
            remainingText = rawLine.substring(0, dateMatcher.start()) + " " + rawLine.substring(dateMatcher.end())
        }

        val numberRegex = Pattern.compile("\\b(\\d{1,3})\\b")
        val numMatcher = numberRegex.matcher(remainingText)
        val extractedNumbers = mutableListOf<Int>()
        while (numMatcher.find()) {
            val num = numMatcher.group(1)?.toIntOrNull()
            if (num != null) {
                extractedNumbers.add(num)
            }
        }

        if (extractedNumbers.size < 4 && dateString == null) {
            return LineParseResult.Ignored
        }

        if (extractedNumbers.size < 5) {
            return LineParseResult.Invalid(
                InvalidLineReport(
                    lineNumber = lineNumber,
                    rawText = rawLine,
                    reason = "Nombre insuffisant de numéros (${extractedNumbers.size}/5 trouvés)"
                )
            )
        }

        val fiveNumbers = extractedNumbers.take(5)

        val outOfBounds = fiveNumbers.filter { it !in 0..90 }
        if (outOfBounds.isNotEmpty()) {
            return LineParseResult.Invalid(
                InvalidLineReport(
                    lineNumber = lineNumber,
                    rawText = rawLine,
                    reason = "Numéro(s) hors bornes [0..90] : ${outOfBounds.joinToString(", ")}"
                )
            )
        }

        if (fiveNumbers.toSet().size != 5) {
            return LineParseResult.Invalid(
                InvalidLineReport(
                    lineNumber = lineNumber,
                    rawText = rawLine,
                    reason = "Doublon détecté parmi les numéros : ${fiveNumbers.joinToString(", ")}"
                )
            )
        }

        val finalDate = dateString ?: "2026-01-01"

        return LineParseResult.Success(
            LotoDraw(
                sourceId = sourceId,
                dateString = finalDate,
                n1 = fiveNumbers[0],
                n2 = fiveNumbers[1],
                n3 = fiveNumbers[2],
                n4 = fiveNumbers[3],
                n5 = fiveNumbers[4]
            )
        )
    }

    private fun normalizeDate(raw: String?): String {
        if (raw.isNullOrBlank()) return "2026-01-01"
        val clean = raw.trim().replace('.', '-').replace('/', '-')
        val parts = clean.split('-')
        if (parts.size != 3) return clean

        return try {
            if (parts[0].length == 4) {
                val y = parts[0].toInt()
                val m = parts[1].padStart(2, '0')
                val d = parts[2].padStart(2, '0')
                "$y-$m-$d"
            } else {
                val d = parts[0].padStart(2, '0')
                val m = parts[1].padStart(2, '0')
                val y = if (parts[2].length == 2) "20" + parts[2] else parts[2]
                "$y-$m-$d"
            }
        } catch (e: Exception) {
            clean
        }
    }
}
