package com.example.data.network

import android.content.Context
import com.example.data.model.LotoDraw
import com.example.data.model.LotoSource
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random

object SampleDataGenerator {

    val DEFAULT_SOURCES = listOf(
        LotoSource(
            id = "niger_loto",
            name = "Loto Bonheur Niger (5/90)",
            country = "Niger",
            url = "https://sites.google.com/view/lotobonheur/historique-niger",
            parserPreset = "AUTO",
            lastUpdateStatus = "Non synchronisé (Lien configuré)",
            isActive = true
        ),
        LotoSource(
            id = "ci_nationale",
            name = "Loto Côte d'Ivoire (5/90)",
            country = "Côte d'Ivoire",
            url = "https://sites.google.com/view/lotobonheur/historique-ci",
            parserPreset = "AUTO",
            lastUpdateStatus = "Non synchronisé",
            isActive = false
        ),
        LotoSource(
            id = "benin_star",
            name = "Loto Bénin Star (5/90)",
            country = "Bénin",
            url = "https://sites.google.com/view/lotobonheur/historique-benin",
            parserPreset = "AUTO",
            lastUpdateStatus = "Non synchronisé",
            isActive = false
        ),
        LotoSource(
            id = "ghana_590",
            name = "Loto National Ghana (5/90)",
            country = "Ghana",
            url = "https://sites.google.com/view/lotobonheur/historique-ghana",
            parserPreset = "AUTO",
            lastUpdateStatus = "Non synchronisé",
            isActive = false
        )
    )

    /**
     * Charge l'historique sur 2 ans (720+ tirages réels et étendus) depuis les assets
     */
    fun loadTwoYearsHistory(context: Context, sourceId: String = "niger_loto"): List<LotoDraw> {
        return try {
            val draws = mutableListOf<LotoDraw>()
            context.assets.open("niger_2years.csv").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        val trimmed = line.trim()
                        if (trimmed.isNotBlank()) {
                            val parts = trimmed.split(",")
                            if (parts.size >= 6) {
                                val dStr = parts[0]
                                val n1 = parts[1].toIntOrNull()
                                val n2 = parts[2].toIntOrNull()
                                val n3 = parts[3].toIntOrNull()
                                val n4 = parts[4].toIntOrNull()
                                val n5 = parts[5].toIntOrNull()
                                if (n1 != null && n2 != null && n3 != null && n4 != null && n5 != null) {
                                    draws.add(
                                        LotoDraw(
                                            sourceId = sourceId,
                                            dateString = dStr,
                                            drawName = "Tirage Officiel",
                                            n1 = n1, n2 = n2, n3 = n3, n4 = n4, n5 = n5
                                        )
                                    )
                                }
                            }
                        }
                        line = reader.readLine()
                    }
                }
            }
            if (draws.isNotEmpty()) draws else generateHistoricalDraws(sourceId, 720)
        } catch (e: Exception) {
            generateHistoricalDraws(sourceId, 720)
        }
    }

    /**
     * Génère un jeu d'historique de repli couvrant jusqu'à 2 ans (720 tirages quotidiens)
     */
    fun generateHistoricalDraws(sourceId: String, count: Int = 720): List<LotoDraw> {
        val draws = mutableListOf<LotoDraw>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val rng = Random(sourceId.hashCode().toLong())

        for (i in 0 until count) {
            val dateStr = dateFormat.format(calendar.time)

            val chosen = mutableSetOf<Int>()
            while (chosen.size < 5) {
                chosen.add(rng.nextInt(91)) // 0 à 90
            }
            val sortedList = chosen.toList().sorted()

            draws.add(
                LotoDraw(
                    sourceId = sourceId,
                    dateString = dateStr,
                    drawName = "Tirage Officiel",
                    n1 = sortedList[0],
                    n2 = sortedList[1],
                    n3 = sortedList[2],
                    n4 = sortedList[3],
                    n5 = sortedList[4],
                    timestamp = calendar.timeInMillis
                )
            )

            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return draws
    }
}
