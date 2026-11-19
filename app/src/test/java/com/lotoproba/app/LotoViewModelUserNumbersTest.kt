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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LotoViewModelUserNumbersTest {

    @Test
    fun `analyzeUserProposedNumbers calculates exact odds and historical co-occurrences`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LotoViewModel(app)

        val draws = listOf(
            LotoDraw(sourceId = "user_test", dateString = "2026-11-01", n1 = 7, n2 = 21, n3 = 45, n4 = 60, n5 = 80),
            LotoDraw(sourceId = "user_test", dateString = "2026-11-02", n1 = 7, n2 = 21, n3 = 33, n4 = 55, n5 = 81),
            LotoDraw(sourceId = "user_test", dateString = "2026-11-03", n1 = 7, n2 = 12, n3 = 45, n4 = 66, n5 = 82),
            LotoDraw(sourceId = "user_test", dateString = "2026-11-04", n1 = 10, n2 = 21, n3 = 30, n4 = 40, n5 = 50)
        )

        val nap2Analysis = viewModel.analyzeUserProposedNumbers(listOf(21, 7), draws)
        assertNotNull(nap2Analysis)
        assertEquals(listOf(7, 21), nap2Analysis!!.numbers)
        assertEquals(4, nap2Analysis.totalDrawsAnalyzed)
        assertEquals(2, nap2Analysis.exactGroupHistoricalHits)
        assertEquals(listOf("2026-11-02", "2026-11-01"), nap2Analysis.exactGroupHitDates)
        assertEquals(0.2442, nap2Analysis.exactHitProbabilityPercent, 0.001)
        assertTrue(nap2Analysis.atLeastOneHitProbabilityPercent > 10.5)
        assertEquals(1, nap2Analysis.pairSynergies.size)
        assertEquals(2, nap2Analysis.pairSynergies.first().coOccurrenceCount)

        val nap1Analysis = viewModel.analyzeUserProposedNumbers(listOf(7), draws)!!
        assertEquals(5.4945, nap1Analysis.exactHitProbabilityPercent, 0.001)
        assertEquals(3, nap1Analysis.numberDetails.first().totalCount)
        assertEquals(1, nap1Analysis.numberDetails.first().currentGap)

        val fullAnalysis = viewModel.analyzeUserProposedNumbers(listOf(7, 21, 45, 60, 80), draws)!!
        assertTrue(fullAnalysis.exactHitOddsText.contains("46 504 458"))
        assertEquals(1, fullAnalysis.exactGroupHistoricalHits)
    }

    @Test
    fun `submitUserNumbersInput validates input and analyzes stored Room draws`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val dao = LotoDatabase.getDatabase(app).lotoDao()
        val sourceId = "test_room_user_numbers"
        runBlocking {
            dao.deleteAllDrawsForSource(sourceId)
            dao.insertDraws(
                listOf(
                    LotoDraw(sourceId = sourceId, dateString = "2026-10-10", n1 = 5, n2 = 17, n3 = 42, n4 = 70, n5 = 90),
                    LotoDraw(sourceId = sourceId, dateString = "2026-10-11", n1 = 5, n2 = 17, n3 = 28, n4 = 61, n5 = 88)
                )
            )
        }

        val viewModel = LotoViewModel(app)
        viewModel.selectSource(sourceId)

        assertFalse(viewModel.submitUserNumbersInput("99"))
        assertNull(viewModel.userNumbersAnalysis.value)

        assertTrue(viewModel.submitUserNumbersInput("17, 5, 42"))

        var attempts = 0
        while (viewModel.userNumbersAnalysis.value?.totalDrawsAnalyzed != 2 && attempts < 40) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(50)
            attempts++
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val analysis = viewModel.userNumbersAnalysis.value
        assertNotNull(analysis)
        assertEquals(listOf(5, 17, 42), analysis!!.numbers)
        assertEquals(2, analysis.totalDrawsAnalyzed)
        assertEquals(1, analysis.exactGroupHistoricalHits)
        assertEquals(2, analysis.partial2PlusHistoricalHits)

        viewModel.removeUserProposedNumber(42)
        assertEquals(listOf(5, 17), viewModel.userProposedNumbers.value)

        var removeAttempts = 0
        while (viewModel.userNumbersAnalysis.value?.numbers != listOf(5, 17) && removeAttempts < 40) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(50)
            removeAttempts++
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertEquals(listOf(5, 17), viewModel.userNumbersAnalysis.value?.numbers)
        assertEquals(2, viewModel.userNumbersAnalysis.value?.exactGroupHistoricalHits)

        viewModel.clearUserProposedNumbers()
        assertTrue(viewModel.userProposedNumbers.value.isEmpty())
        assertNull(viewModel.userNumbersAnalysis.value)
    }
}
