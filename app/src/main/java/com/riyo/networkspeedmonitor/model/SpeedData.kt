package com.riyo.networkspeedmonitor.model

import android.net.TrafficStats
import android.os.SystemClock
import java.util.Locale

/**
 * Encapsulates the network speed readings in decimal MB/s (1 MB = 1,000,000 bytes).
 */
data class SpeedData(
    val downloadMBps: Double = 0.0,
    val uploadMBps: Double = 0.0,
    val isSupported: Boolean = true
) {
    fun formatDownload(): String = String.format(Locale.US, "%.2f", downloadMBps)
    fun formatUpload(): String = String.format(Locale.US, "%.2f", uploadMBps)
}

/**
 * Calculates download and upload throughput by sampling TrafficStats total byte counters
 * and accurately computing elapsed time using SystemClock.elapsedRealtime().
 */
class SpeedCalculator {
    private var lastRxBytes: Long = TrafficStats.UNSUPPORTED.toLong()
    private var lastTxBytes: Long = TrafficStats.UNSUPPORTED.toLong()
    private var lastTimeMs: Long = 0L

    fun reset() {
        lastRxBytes = TrafficStats.UNSUPPORTED.toLong()
        lastTxBytes = TrafficStats.UNSUPPORTED.toLong()
        lastTimeMs = 0L
    }

    fun calculateSpeed(): SpeedData {
        val currentRx = TrafficStats.getTotalRxBytes()
        val currentTx = TrafficStats.getTotalTxBytes()
        val currentTimeMs = SystemClock.elapsedRealtime()

        // Check if traffic counters are supported on this device
        if (currentRx == TrafficStats.UNSUPPORTED.toLong() || currentTx == TrafficStats.UNSUPPORTED.toLong()) {
            return SpeedData(downloadMBps = 0.0, uploadMBps = 0.0, isSupported = false)
        }

        // On initial reading, seed reference points and return 0.0 MB/s
        if (lastTimeMs == 0L || lastRxBytes < 0 || lastTxBytes < 0) {
            lastRxBytes = currentRx
            lastTxBytes = currentTx
            lastTimeMs = currentTimeMs
            return SpeedData(downloadMBps = 0.0, uploadMBps = 0.0, isSupported = true)
        }

        val elapsedMs = currentTimeMs - lastTimeMs
        if (elapsedMs <= 0) {
            return SpeedData(downloadMBps = 0.0, uploadMBps = 0.0, isSupported = true)
        }

        val rxDelta = currentRx - lastRxBytes
        val txDelta = currentTx - lastTxBytes

        // Handle counter resets, negative deltas, or reboot rollovers safely
        val safeRxDelta = if (rxDelta < 0) 0L else rxDelta
        val safeTxDelta = if (txDelta < 0) 0L else txDelta

        lastRxBytes = currentRx
        lastTxBytes = currentTx
        lastTimeMs = currentTimeMs

        // Decimal MB/s: 1 MB = 1,000,000 bytes
        // Speed = (deltaBytes / 1_000_000.0) / (elapsedMs / 1000.0) = deltaBytes / (elapsedMs * 1000.0)
        val downloadMBps = safeRxDelta.toDouble() / (elapsedMs * 1000.0)
        val uploadMBps = safeTxDelta.toDouble() / (elapsedMs * 1000.0)

        val cleanDownload = if (downloadMBps < 0.0 || downloadMBps.isNaN()) 0.0 else downloadMBps
        val cleanUpload = if (uploadMBps < 0.0 || uploadMBps.isNaN()) 0.0 else uploadMBps

        return SpeedData(
            downloadMBps = cleanDownload,
            uploadMBps = cleanUpload,
            isSupported = true
        )
    }
}
