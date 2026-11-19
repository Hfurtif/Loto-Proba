package com.lotoproba.app

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.lotoproba.app.data.db.LotoDatabase
import com.lotoproba.app.data.model.LotoDraw
import com.lotoproba.app.ui.LotoViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LotoViewModelChiSquareTest {

    @Test
    fun `runChiSquareEquityTest confirms uniformity on balanced distribution and detects bias on skewed draws`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LotoViewModel(app)

        val fewDraws = (1..5).map { idx ->
            LotoDraw(sourceId = "chi_few", dateString = "2026-11-0$idx", n1 = 1, n2 = 2, n3 = 3, n4 = 4, n5 = 5)
        }
        val insufficient = viewModel.runChiSquareEquityTest(fewDraws)
        assertFalse(insufficient.hasSufficientData)
        assertEquals(5, insufficient.totalDrawsAnalyzed)

        val uniformDraws = (0 until 91).map { day ->
            val n1 = (day * 5) % 91
            val n2 = (day * 5 + 1) % 91
            val n3 = (day * 5 + 2) % 91
            val n4 = (day * 5 + 3) % 91
            val n5 = (day * 5 + 4) % 91
            LotoDraw(
                sourceId = "chi_uniform",
                dateString = "2026-01-${(day + 1).toString().padStart(3, '0')}",
                n1 = n1,
                n2 = n2,
                n3 = n3,
                n4 = n4,
                n5 = n5
            )
        }
        val uniformResult = viewModel.runChiSquareEquityTest(uniformDraws)
        assertTrue(uniformResult.hasSufficientData)
        assertEquals(91, uniformResult.totalDrawsAnalyzed)
        assertEquals(90, uniformResult.degreesOfFreedom)
        assertEquals(5.0, uniformResult.expectedFrequencyPerNumber, 0.001)
        assertEquals(5, uniformResult.minObservedFrequency)
        assertEquals(5, uniformResult.maxObservedFrequency)
        assertEquals(0.0, uniformResult.chiSquareValue, 0.001)
        assertTrue(uniformResult.isCompatibleWithRandom)
        assertEquals(uniformResult, viewModel.chiSquareResult.value)

        val biasedDraws = (1..40).map { day ->
            LotoDraw(
                sourceId = "chi_biased",
                dateString = "2026-02-${day.toString().padStart(2, '0')}",
                n1 = 7,
                n2 = 14,
                n3 = 21,
                n4 = 28,
                n5 = 35
            )
        }
        val biasedResult = viewModel.runChiSquareEquityTest(biasedDraws)
        assertTrue(biasedResult.hasSufficientData)
        assertTrue(biasedResult.chiSquareValue > biasedResult.criticalValue005)
        assertFalse(biasedResult.isCompatibleWithRandom)
        assertTrue(biasedResult.pValueApprox < 0.05)
        assertEquals(biasedResult, viewModel.chiSquareResult.value)
    }

    @Test
    fun `runChiSquareEquityTestOnFrequencies and runChiSquareEquityTestFromHistory work with uniform baseline`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val dao = LotoDatabase.getDatabase(app).lotoDao()
        val sourceId = "test_room_chi_square"

        runBlocking {
            dao.deleteAllDrawsForSource(sourceId)
            val biasedRoomDraws = (1..20).map { day ->
                LotoDraw(
                    sourceId = sourceId,
                    dateString = "2026-09-${day.toString().padStart(2, '0')}",
                    n1 = 1,
                    n2 = 2,
                    n3 = 3,
                    n4 = 4,
                    n5 = 5
                )
            }
            dao.insertDraws(biasedRoomDraws)
        }

        val viewModel = LotoViewModel(app)

        val uniformFreqMap = (0..90).associateWith { 10 }
        val mapResult = viewModel.runChiSquareEquityTestOnFrequencies(uniformFreqMap)
        assertTrue(mapResult.hasSufficientData)
        assertEquals(0.0, mapResult.chiSquareValue, 0.001)
        assertTrue(mapResult.isCompatibleWithRandom)

        viewModel.selectSource(sourceId)
        viewModel.runChiSquareEquityTestFromHistory(sourceId)

        var attempts = 0
        while ((viewModel.chiSquareResult.value.totalDrawsAnalyzed != 20 || viewModel.chiSquareResult.value.isCompatibleWithRandom) && attempts < 40) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(50)
            attempts++
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val roomResult = viewModel.chiSquareResult.value
        assertTrue(roomResult.hasSufficientData)
        assertEquals(20, roomResult.totalDrawsAnalyzed)
        assertFalse(roomResult.isCompatibleWithRandom)
        assertTrue(roomResult.chiSquareValue > roomResult.criticalValue005)
    }
}
