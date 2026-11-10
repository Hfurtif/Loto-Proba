package com.example

import com.example.data.model.LotoDraw
import com.example.data.network.WebScraper
import com.example.stats.LotoStatsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LotoProbaUnitTest {

    @Test
    fun draw_validation_strictRules() {
        // Valid: 5 distinct numbers between 0 and 90
        val validDraw = LotoDraw(
            sourceId = "test",
            dateString = "2026-11-08",
            n1 = 0, n2 = 14, n3 = 45, n4 = 72, n5 = 90
        )
        assertTrue(validDraw.isValid())

        // Invalid: Duplicate numbers
        val duplicateDraw = LotoDraw(
            sourceId = "test",
            dateString = "2026-11-08",
            n1 = 12, n2 = 12, n3 = 45, n4 = 72, n5 = 90
        )
        assertFalse(duplicateDraw.isValid())

        // Invalid: Number > 90
        val outOfBoundsDraw = LotoDraw(
            sourceId = "test",
            dateString = "2026-11-08",
            n1 = 5, n2 = 14, n3 = 45, n4 = 72, n5 = 91
        )
        assertFalse(outOfBoundsDraw.isValid())
    }

    @Test
    fun chiSquare_uniformityTest_calculated() {
        val testDraws = (1..100).map { i ->
            LotoDraw(
                sourceId = "test",
                dateString = "2026-01-${(i % 28 + 1).toString().padStart(2, '0')}",
                n1 = (i * 3) % 91,
                n2 = (i * 7 + 1) % 91,
                n3 = (i * 11 + 2) % 91,
                n4 = (i * 13 + 3) % 91,
                n5 = (i * 17 + 4) % 91
            )
        }

        val result = LotoStatsCalculator.calculateChiSquareTest(testDraws)
        assertEquals(90, result.degreesOfFreedom)
        assertTrue(result.chiSquareValue >= 0.0)
        assertTrue(result.plainFrenchExplanation.isNotBlank())
    }

    @Test
    fun combinations_generatesExactly5Combinations() {
        val sampleDraws = (1..50).map { i ->
            val set = mutableSetOf<Int>()
            var step = i
            while (set.size < 5) {
                set.add(step % 91)
                step += 7
            }
            val list = set.toList()
            LotoDraw(
                sourceId = "test",
                dateString = "2026-01-01",
                n1 = list[0], n2 = list[1], n3 = list[2], n4 = list[3], n5 = list[4]
            )
        }

        val stats = LotoStatsCalculator.calculateNumberStats(sampleDraws)
        val combos = LotoStatsCalculator.generate5ProposedCombinations(stats, 40f, 30f, 30f)

        assertEquals(5, combos.size)
        combos.forEach { combo ->
            assertEquals(5, combo.numbers.size)
            assertEquals(5, combo.numbers.toSet().size)
            assertTrue(combo.numbers.all { it in 0..90 })
            assertTrue(combo.trueProbabilityText.contains("46 504 458"))
        }
    }

    @Test
    fun scraper_parser_validatesLinesAndReportsErrors() {
        val scraper = WebScraper()
        val text = """
            2026-11-05 : 10, 20, 30, 40, 50
            2026-11-04 : 05, 05, 15, 25, 35
            2026-11-03 : 01, 02, 03, 04, 99
        """.trimIndent()

        val report = scraper.parseDraws(text, "test")
        assertEquals(1, report.validCount)
        assertEquals(2, report.invalidCount)
    }

    @Test
    fun scraper_parser_googleSitesNigerFormat() {
        val scraper = WebScraper()
        val text = """
            <p>Samedi 07 novembre 2026</p>
            <p>RESULTAT LOTO NIGER // 53 - 59 - 17 - 29 - 51 // . - . - .</p>
            <p>Vendredi 06 novembre 2026</p>
            <p>RESULTAT LOTO NIGER // 03 - 14 - 35 - 20 - 55 // . - . - .</p>
            <p>Jeudi 05 novembre 2026</p>
            <p>RESULTAT LOTO NIGER // 41 - 65 - 47 - 99 - 90 // . - . - .</p>
        """.trimIndent()

        val report = scraper.parseDraws(text, "niger_loto")
        assertEquals(2, report.validCount)
        assertEquals(1, report.invalidCount) // 99 is invalid
        assertEquals("2026-11-07", report.parsedDraws[0].dateString)
        assertEquals(listOf(17, 29, 51, 53, 59), report.parsedDraws[0].getNumbersList())
    }
}
