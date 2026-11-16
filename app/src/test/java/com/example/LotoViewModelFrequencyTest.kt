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
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LotoViewModelFrequencyTest {

    @Test
    fun `processStoredDrawsFrequency calculates exact counts for 01 to 90`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LotoViewModel(app)

        val draws = listOf(
            LotoDraw(sourceId = "test_src", dateString = "2026-11-10", n1 = 1, n2 = 5, n3 = 17, n4 = 45, n5 = 90),
            LotoDraw(sourceId = "test_src", dateString = "2026-11-11", n1 = 1, n2 = 17, n3 = 23, n4 = 60, n5 = 90),
            LotoDraw(sourceId = "test_src", dateString = "2026-11-12", n1 = 0, n2 = 5, n3 = 17, n4 = 77, n5 = 88)
        )

        val frequencyMap = viewModel.processStoredDrawsFrequency(draws)

        assertEquals(90, frequencyMap.size)
        assertFalse(frequencyMap.containsKey("00"))
        assertTrue(frequencyMap.containsKey("01"))
        assertTrue(frequencyMap.containsKey("90"))

        assertEquals(2, frequencyMap["01"])
        assertEquals(2, frequencyMap["05"])
        assertEquals(3, frequencyMap["17"])
        assertEquals(1, frequencyMap["23"])
        assertEquals(1, frequencyMap["45"])
        assertEquals(1, frequencyMap["60"])
        assertEquals(1, frequencyMap["77"])
        assertEquals(1, frequencyMap["88"])
        assertEquals(2, frequencyMap["90"])
        assertEquals(0, frequencyMap["02"])
        assertEquals(0, frequencyMap["89"])

        val list = viewModel.numberFrequencies01To90.value
        assertEquals(90, list.size)
        assertEquals("01", list.first().formattedNumber)
        assertEquals("90", list.last().formattedNumber)
        assertEquals(100.0, list.first { it.number == 17 }.frequencyPercent, 0.01)
    }

    @Test
    fun `calculateNumberFrequencies01To90 processes stored Room draws`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val dao = LotoDatabase.getDatabase(app).lotoDao()
        val sourceId = "test_room_freq"
        runBlocking {
            dao.deleteAllDrawsForSource(sourceId)
            dao.insertDraws(
                listOf(
                    LotoDraw(sourceId = sourceId, dateString = "2026-11-01", n1 = 1, n2 = 10, n3 = 20, n4 = 30, n5 = 90),
                    LotoDraw(sourceId = sourceId, dateString = "2026-11-02", n1 = 1, n2 = 15, n3 = 25, n4 = 35, n5 = 90)
                )
            )
        }

        val viewModel = LotoViewModel(app)
        viewModel.selectSource(sourceId)
        viewModel.calculateNumberFrequencies01To90(sourceId)

        var attempts = 0
        while (viewModel.numberFrequencies01To90.value.first { it.number == 1 }.count != 2 && attempts < 40) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(50)
            attempts++
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val freqList = viewModel.numberFrequencies01To90.value
        assertEquals(90, freqList.size)
        assertEquals(2, freqList.first { it.formattedNumber == "01" }.count)
        assertEquals(2, freqList.first { it.formattedNumber == "90" }.count)
        assertEquals(1, freqList.first { it.formattedNumber == "10" }.count)
        assertEquals(0, freqList.first { it.formattedNumber == "02" }.count)
    }
}
