package com.example

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.LotoDatabase
import com.example.data.model.LotoDraw
import com.example.ui.LotoViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LotoViewModelGapsAndColdTest {

    @Test
    fun `processHistoricalDrawsForGapsAndCold computes exact gaps and least recently drawn numbers`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LotoViewModel(app)

        // 7 tirages chronologiques ; avec fenêtre récente = 5, les 5 derniers sont 2026-11-03 à 2026-11-07
        val draws = listOf(
            LotoDraw(sourceId = "test_gap", dateString = "2026-11-01", n1 = 10, n2 = 20, n3 = 30, n4 = 40, n5 = 50),
            LotoDraw(sourceId = "test_gap", dateString = "2026-11-02", n1 = 10, n2 = 21, n3 = 31, n4 = 41, n5 = 51),
            LotoDraw(sourceId = "test_gap", dateString = "2026-11-03", n1 = 1, n2 = 2, n3 = 3, n4 = 4, n5 = 5),
            LotoDraw(sourceId = "test_gap", dateString = "2026-11-04", n1 = 1, n2 = 2, n3 = 3, n4 = 4, n5 = 6),
            LotoDraw(sourceId = "test_gap", dateString = "2026-11-05", n1 = 1, n2 = 2, n3 = 3, n4 = 7, n5 = 8),
            LotoDraw(sourceId = "test_gap", dateString = "2026-11-06", n1 = 1, n2 = 9, n3 = 11, n4 = 12, n5 = 13),
            LotoDraw(sourceId = "test_gap", dateString = "2026-11-07", n1 = 1, n2 = 14, n3 = 15, n4 = 16, n5 = 17)
        )

        val analysis = viewModel.processHistoricalDrawsForGapsAndCold(
            historicalDraws = draws,
            recentWindowSize = 5,
            topLimit = 10
        )

        assertEquals(7, analysis.totalDrawsAnalyzed)
        assertEquals(5, analysis.recentWindowSize)
        assertEquals(91, analysis.numberGaps.size)

        // Numéro 1 : sorti au tout dernier tirage (2026-11-07) -> retard = 0, 5 apparitions récentes
        val stat1 = analysis.numberGaps.first { it.number == 1 }
        assertEquals(0, stat1.drawsSinceLastAppearance)
        assertEquals("2026-11-07", stat1.lastSeenDate)
        assertEquals(5, stat1.recentAppearances)
        assertEquals(5, stat1.totalAppearances)
        assertFalse(stat1.neverAppeared)

        // Numéro 9 : sorti à l'avant-dernier tirage (2026-11-06) -> retard = 1
        val stat9 = analysis.numberGaps.first { it.number == 9 }
        assertEquals(1, stat9.drawsSinceLastAppearance)
        assertEquals("2026-11-06", stat9.lastSeenDate)
        assertEquals(1, stat9.recentAppearances)

        // Numéro 10 : sorti au 2026-11-01 et 2026-11-02 (index 1 sur 7) -> 5 tirages écoulés, 0 récent
        val stat10 = analysis.numberGaps.first { it.number == 10 }
        assertEquals(5, stat10.drawsSinceLastAppearance)
        assertEquals("2026-11-02", stat10.lastSeenDate)
        assertEquals(0, stat10.recentAppearances)
        assertEquals(2, stat10.totalAppearances)
        assertFalse(stat10.neverAppeared)

        // Numéro 20 : sorti uniquement au 2026-11-01 (index 0) -> 6 tirages écoulés, 0 récent
        val stat20 = analysis.numberGaps.first { it.number == 20 }
        assertEquals(6, stat20.drawsSinceLastAppearance)
        assertEquals("2026-11-01", stat20.lastSeenDate)
        assertEquals(0, stat20.recentAppearances)

        // Numéro 89 : jamais sorti -> retard = 7, neverAppeared = true, lastSeenDate = null
        val stat89 = analysis.numberGaps.first { it.number == 89 }
        assertEquals(7, stat89.drawsSinceLastAppearance)
        assertNull(stat89.lastSeenDate)
        assertEquals(0, stat89.recentAppearances)
        assertTrue(stat89.neverAppeared)

        // Les numéros les moins sortis récemment ont 0 apparition récente et sont triés par retard DESC
        assertEquals(0, analysis.leastRecentlyDrawn.first().recentAppearances)
        assertEquals(7, analysis.leastRecentlyDrawn.first().drawsSinceLastAppearance)
        assertEquals(analysis, viewModel.gapAndColdAnalysis.value)
    }

    @Test
    fun `calculateGapsAndLeastRecentFromHistory loads Room draws and updates gap state`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val dao = LotoDatabase.getDatabase(app).lotoDao()
        val sourceId = "test_room_gaps_cold"
        runBlocking {
            dao.deleteAllDrawsForSource(sourceId)
            dao.insertDraws(
                listOf(
                    LotoDraw(sourceId = sourceId, dateString = "2026-10-01", n1 = 4, n2 = 8, n3 = 12, n4 = 16, n5 = 20),
                    LotoDraw(sourceId = sourceId, dateString = "2026-10-02", n1 = 5, n2 = 10, n3 = 15, n4 = 25, n5 = 30),
                    LotoDraw(sourceId = sourceId, dateString = "2026-10-03", n1 = 6, n2 = 11, n3 = 22, n4 = 33, n5 = 44),
                    LotoDraw(sourceId = sourceId, dateString = "2026-10-04", n1 = 7, n2 = 14, n3 = 21, n4 = 28, n5 = 35),
                    LotoDraw(sourceId = sourceId, dateString = "2026-10-05", n1 = 1, n2 = 2, n3 = 3, n4 = 4, n5 = 90)
                )
            )
        }

        val viewModel = LotoViewModel(app)
        viewModel.selectSource(sourceId)
        viewModel.calculateGapsAndLeastRecentFromHistory(
            sourceId = sourceId,
            recentWindowSize = 5,
            topLimit = 5
        )

        var attempts = 0
        while (viewModel.gapAndColdAnalysis.value.totalDrawsAnalyzed != 5 && attempts < 40) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(50)
            attempts++
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val result = viewModel.gapAndColdAnalysis.value
        assertEquals(5, result.totalDrawsAnalyzed)
        assertEquals(5, result.longestGaps.size)
        assertEquals(5, result.leastRecentlyDrawn.size)

        // Numéro 90 et numéro 4 sont dans le dernier tirage (2026-10-05) -> retard = 0
        assertEquals(0, result.numberGaps.first { it.number == 90 }.drawsSinceLastAppearance)
        assertEquals(0, result.numberGaps.first { it.number == 4 }.drawsSinceLastAppearance)

        // Numéro 8 était uniquement dans le premier tirage (2026-10-01) -> retard = 4
        assertEquals(4, result.numberGaps.first { it.number == 8 }.drawsSinceLastAppearance)

        // Numéro 5 était dans le 2e tirage (2026-10-02) -> retard = 3
        assertEquals(3, result.numberGaps.first { it.number == 5 }.drawsSinceLastAppearance)

        // Numéro 85 jamais sorti -> retard = 5
        assertEquals(5, result.numberGaps.first { it.number == 85 }.drawsSinceLastAppearance)
    }
}
