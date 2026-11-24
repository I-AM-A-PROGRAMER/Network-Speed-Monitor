package com.riyosup.networkspeedmonitor

import com.riyosup.networkspeedmonitor.model.SpeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeedDataTest {

    @Test
    fun testDecimalSpeedCalculation() {
        // Verification that 1 MB = 1,000,000 bytes (decimal MB/s)
        val rxDeltaBytes = 4_250_000L
        val elapsedMs = 1000L
        val downloadMBps = rxDeltaBytes.toDouble() / (elapsedMs * 1000.0)

        assertEquals(4.25, downloadMBps, 0.0001)

        val speedData = SpeedData(
            downloadMBps = downloadMBps,
            uploadMBps = 0.18,
            isSupported = true
        )

        assertEquals("4.25", speedData.formatDownload())
        assertEquals("0.18", speedData.formatUpload())
        assertTrue(speedData.isSupported)
    }

    @Test
    fun testUploadDecimalCalculation() {
        val txDeltaBytes = 180_000L
        val elapsedMs = 1000L
        val uploadMBps = txDeltaBytes.toDouble() / (elapsedMs * 1000.0)

        assertEquals(0.18, uploadMBps, 0.0001)
    }

    @Test
    fun testZeroAndSmallDeltas() {
        val speedData = SpeedData(
            downloadMBps = 0.0,
            uploadMBps = 0.004,
            isSupported = true
        )

        assertEquals("0.00", speedData.formatDownload())
        assertEquals("0.00", speedData.formatUpload())
    }
}
