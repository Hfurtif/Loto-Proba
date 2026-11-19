package com.lotoproba.app

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.lotoproba.app.data.db.LotoDatabase
import com.lotoproba.app.data.model.LotoDraw
import com.lotoproba.app.ui.LotoViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LotoViewModelPairsTriosTest {

    @Test
    fun `processHistoricalDrawsForPairsAndTrios identifies most frequent pairs and trios deterministically`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LotoViewModel(app)

        val draws = listOf(
            LotoDraw(sourceId = "test_pt", dateString = "2026-11-01", n1 = 5, n2 = 12, n3 = 27, n4 = 40, n5 = 88),
            LotoDraw(sourceId = "test_pt", dateString = "2026-11-02", n1 = 5, n2 = 12, n3 = 27, n4 = 51, n5 = 70),
            LotoDraw(sourceId = "test_pt", dateString = "2026-11-03", n1 = 5, n2 = 12, n3 = 27, n4 = 61, n5 = 79),
            LotoDraw(sourceId = "test_pt", dateString = "2026-11-04", n1 = 5, n2 = 12, n3 = 33, n4 = 44, n5 = 89),
            LotoDraw(sourceId = "test_pt", dateString = "2026-11-05", n1 = 2, n2 = 14, n3 = 33, n4 = 44, n5 = 90)
        )

        val analysis = viewModel.processHistoricalDrawsForPairsAndTrios(draws, limit = 5)

        assertEquals(5, analysis.totalDrawsAnalyzed)
        assertEquals(5, analysis.topPairs.size)
        assertEquals(5, analysis.topTrios.size)

        val bestPair = analysis.topPairs.first()
        assertEquals(5, bestPair.n1)
        assertEquals(12, bestPair.n2)
        assertEquals(4, bestPair.count)
        assertEquals(80.0, bestPair.frequencyPercent, 0.01)

        val bestTrio = analysis.topTrios.first()
        assertEquals(5, bestTrio.n1)
        assertEquals(12, bestTrio.n2)
        assertEquals(27, bestTrio.n3)
        assertEquals(3, bestTrio.count)
        assertEquals(60.0, bestTrio.frequencyPercent, 0.01)

        assertEquals(analysis, viewModel.pairsAndTriosAnalysis.value)
        assertEquals(5, viewModel.pairTrioLimit.value)
    }

    @Test
    fun `analyzePairsAndTriosFromHistory loads stored Room draws and respects limit`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val dao = LotoDatabase.getDatabase(app).lotoDao()
        val sourceId = "test_room_pairs_trios"
        runBlocking {
            dao.deleteAllDrawsForSource(sourceId)
            dao.insertDraws(
                listOf(
                    LotoDraw(sourceId = sourceId, dateString = "2026-10-01", n1 = 3, n2 = 18, n3 = 42, n4 = 66, n5 = 81),
                    LotoDraw(sourceId = sourceId, dateString = "2026-10-02", n1 = 3, n2 = 18, n3 = 42, n4 = 55, n5 = 73),
                    LotoDraw(sourceId = sourceId, dateString = "2026-10-03", n1 = 3, n2 = 18, n3 = 29, n4 = 55, n5 = 84)
                )
            )
        }

        val viewModel = LotoViewModel(app)
        viewModel.selectSource(sourceId)
        viewModel.analyzePairsAndTriosFromHistory(sourceId = sourceId, limit = 3)

        var attempts = 0
        while (viewModel.pairsAndTriosAnalysis.value.totalDrawsAnalyzed != 3 && attempts < 40) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(50)
            attempts++
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val result = viewModel.pairsAndTriosAnalysis.value
        assertEquals(3, result.totalDrawsAnalyzed)
        assertEquals(3, result.topPairs.size)
        assertEquals(3, result.topTrios.size)

        val topPair = result.topPairs.first()
        assertEquals(3, topPair.n1)
        assertEquals(18, topPair.n2)
        assertEquals(3, topPair.count)
        assertEquals(100.0, topPair.frequencyPercent, 0.01)

        val topTrio = result.topTrios.first()
        assertEquals(3, topTrio.n1)
        assertEquals(18, topTrio.n2)
        assertEquals(42, topTrio.n3)
        assertEquals(2, topTrio.count)
        assertTrue(topTrio.frequencyPercent > 66.0)
    }
}
