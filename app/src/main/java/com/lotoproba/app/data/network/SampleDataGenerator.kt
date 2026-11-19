package com.lotoproba.app.data.network

import android.content.Context
import com.lotoproba.app.data.model.LotoDraw
import com.lotoproba.app.data.model.LotoSource
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random

object SampleDataGenerator {

    const val DEFAULT_LOCAL_SOURCE_ID = "niger_loto"
    const val LOCAL_CSV_ASSET_NAME = "niger_2years.csv"
    const val LOCAL_CSV_ASSET_URI = "asset://$LOCAL_CSV_ASSET_NAME"

    val DEFAULT_SOURCES = listOf(
        LotoSource(
            id = DEFAULT_LOCAL_SOURCE_ID,
            name = "Historique Niger (CSV intégré)",
            country = "Niger",
            url = LOCAL_CSV_ASSET_URI,
            parserPreset = "CSV",
            lastUpdateStatus = "Données locales intégrées (hors ligne)",
            isActive = true
        )
    )

    /**
     * Charge l'historique embarqué depuis assets/niger_2years.csv sans doublons de dates.
     */
    fun loadTwoYearsHistory(
        context: Context,
        sourceId: String = DEFAULT_LOCAL_SOURCE_ID
    ): List<LotoDraw> {
        return try {
            val draws = mutableListOf<LotoDraw>()
            val seenDates = mutableSetOf<String>()
            context.assets.open(LOCAL_CSV_ASSET_NAME).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        val trimmed = line.trim()
                        if (trimmed.isNotBlank()) {
                            val parts = trimmed.split(",", ";")
                            if (parts.size >= 6) {
                                val dStr = parts[0].trim()
                                val n1 = parts[1].trim().toIntOrNull()
                                val n2 = parts[2].trim().toIntOrNull()
                                val n3 = parts[3].trim().toIntOrNull()
                                val n4 = parts[4].trim().toIntOrNull()
                                val n5 = parts[5].trim().toIntOrNull()
                                if (dStr.isNotBlank() &&
                                    dStr !in seenDates &&
                                    n1 != null && n2 != null && n3 != null && n4 != null && n5 != null
                                ) {
                                    val candidate = LotoDraw(
                                        sourceId = sourceId,
                                        dateString = dStr,
                                        drawName = "Tirage Officiel",
                                        n1 = n1,
                                        n2 = n2,
                                        n3 = n3,
                                        n4 = n4,
                                        n5 = n5
                                    )
                                    if (candidate.isValid()) {
                                        seenDates.add(dStr)
                                        draws.add(candidate)
                                    }
                                }
                            }
                        }
                        line = reader.readLine()
                    }
                }
            }
            if (draws.isNotEmpty()) draws else generateHistoricalDraws(sourceId, 720)
        } catch (_: Exception) {
            generateHistoricalDraws(sourceId, 720)
        }
    }

    /**
     * Génère un jeu de repli local si la lecture du CSV embarqué échoue.
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
