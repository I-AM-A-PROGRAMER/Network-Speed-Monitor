package com.riyo.networkspeedmonitor.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.riyo.networkspeedmonitor.MainActivity
import com.riyo.networkspeedmonitor.R
import com.riyo.networkspeedmonitor.model.SpeedData
import com.riyo.networkspeedmonitor.service.NetworkSpeedMonitorService
import com.riyo.networkspeedmonitor.util.SpeedIconGenerator
import java.util.Locale

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "network_speed_monitor_live"
        const val NOTIFICATION_ID = 1001
        private const val CHANNEL_NAME = "Network Speed Monitor"
        private const val CHANNEL_DESC = "Continuous device-wide network speed monitoring and live updates"
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Remove legacy channel if migrating from previous builds
        try {
            notificationManager.deleteNotificationChannel("network_speed_monitor_channel")
        } catch (_: Exception) { }

        val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
        if (existingChannel == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESC
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(speed: SpeedData): Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, NetworkSpeedMonitorService::class.java).apply {
            action = NetworkSpeedMonitorService.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedDown = speed.formatDownload()
        val formattedUp = speed.formatUpload()

        // Short critical text string (under 7 characters) displayed inside the Android 16/17 Live Update / Samsung Now Bar status bar chip
        val shortCriticalText = when {
            speed.downloadMBps >= 10.0 -> String.format(Locale.US, "%.1fM", speed.downloadMBps)
            speed.downloadMBps >= 0.01 -> String.format(Locale.US, "%.2fM", speed.downloadMBps)
            speed.uploadMBps >= 0.01 -> String.format(Locale.US, "↑%.1fM", speed.uploadMBps)
            else -> "0.0M"
        }

        // Dynamically drawn small icon with current speed digits to ensure live values are visible in the status bar
        val speedIcon = try {
            SpeedIconGenerator.generateSpeedIcon(speed)
        } catch (_: Exception) {
            null
        }

        val extras = Bundle().apply {
            putString("android.shortCriticalText", shortCriticalText)
            putCharSequence("android.shortCriticalText", shortCriticalText)
            putString("android.subText", "$formattedDown MB/s")
            putBoolean("com.samsung.android.notification.nowbar", true)
            putBoolean("com.samsung.android.notification.liveUpdate", true)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .apply {
                if (speedIcon != null) {
                    setSmallIcon(speedIcon)
                } else {
                    setSmallIcon(R.drawable.ic_app_logo)
                }
            }
            .setContentTitle("Network Speed Monitor")
            .setContentText("↓ $formattedDown MB/s   ↑ $formattedUp MB/s")
            .setSubText("$formattedDown MB/s")
            .setShortCriticalText(shortCriticalText)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setColorized(false)
            .setGroupSummary(false)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .addExtras(extras)
            .setContentIntent(contentIntent)
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_stop,
                    "Stop",
                    stopPendingIntent
                ).build()
            )
            .setRequestPromotedOngoing(true)

        // On Android 17 (API 37+), use NotificationCompat.MetricStyle with live Download & Upload metrics
        if (Build.VERSION.SDK_INT >= 37) {
            val downloadMetric = NotificationCompat.Metric(
                NotificationCompat.Metric.FixedFloat(speed.downloadMBps.toFloat(), "MB/s", 2, 2),
                "Download"
            )
            val uploadMetric = NotificationCompat.Metric(
                NotificationCompat.Metric.FixedFloat(speed.uploadMBps.toFloat(), "MB/s", 2, 2),
                "Upload"
            )
            val metricStyle = NotificationCompat.MetricStyle()
                .addMetric(downloadMetric)
                .addMetric(uploadMetric)
                .setCriticalMetric(0) // Download is designated as critical metric by default

            builder.setStyle(metricStyle)
        } else {
            // Fallback for API < 37
            val bigTextStyle = NotificationCompat.BigTextStyle()
                .setBigContentTitle("Network Speed Monitor")
                .bigText("Download: $formattedDown MB/s\nUpload: $formattedUp MB/s")
            builder.setStyle(bigTextStyle)
        }

        return builder.build()
    }

    fun updateNotification(speed: SpeedData) {
        val notification = buildNotification(speed)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
