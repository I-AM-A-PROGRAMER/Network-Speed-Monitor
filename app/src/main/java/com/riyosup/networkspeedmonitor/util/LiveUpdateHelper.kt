package com.riyosup.networkspeedmonitor.util

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

enum class LiveUpdateStatus {
    ALLOWED,
    DISABLED,
    NOT_SUPPORTED_OS
}

object LiveUpdateHelper {

    val isSamsungDevice: Boolean
        get() = Build.MANUFACTURER.equals("samsung", ignoreCase = true)

    /**
     * Inspects whether the OS supports Live Updates (API 36+) and checks
     * NotificationManager.canPostPromotedNotifications().
     */
    fun getPromotionStatus(context: Context): LiveUpdateStatus {
        if (Build.VERSION.SDK_INT < 36) {
            return LiveUpdateStatus.NOT_SUPPORTED_OS
        }

        return try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.canPostPromotedNotifications()) {
                LiveUpdateStatus.ALLOWED
            } else {
                LiveUpdateStatus.DISABLED
            }
        } catch (e: Exception) {
            LiveUpdateStatus.NOT_SUPPORTED_OS
        }
    }

    /**
     * Attempts to open the OS Live Update / Promoted Notifications settings page for this app.
     * Gracefully falls back through alternative settings targets if specific intent is unavailable.
     */
    fun openLiveUpdateSettings(context: Context): Boolean {
        val packageName = context.packageName

        // 1. Primary standard action on Android 16/17 (API 36+)
        if (Build.VERSION.SDK_INT >= 36) {
            try {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return true
            } catch (_: Exception) { }

            // 2. Fallback to alternate action constant if platform alias exists
            try {
                val intent = Intent("android.settings.MANAGE_APP_PROMOTED_NOTIFICATIONS").apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return true
            } catch (_: Exception) { }
        }

        // 3. Fallback to App Notification Settings
        try {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return true
        } catch (_: Exception) { }

        // 4. Fallback to Application Details Settings
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return true
        } catch (_: Exception) { }

        return false
    }

    /**
     * Opens Samsung One UI Now Bar & Lock Screen settings.
     */
    fun openSamsungNowBarSettings(context: Context): Boolean {
        val samsungIntents = listOf(
            Intent("com.samsung.android.app.lockscreen.ACTION_NOW_BAR_SETTINGS"),
            Intent("com.samsung.android.app.lockscreen.NOW_BAR_SETTINGS"),
            Intent("com.samsung.settings.LOCKSCREEN_AND_SECURITY"),
            Intent("com.samsung.settings.LOCKSCREEN"),
            Intent("android.settings.LOCKSCREEN_SETTINGS"),
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        )

        for (intent in samsungIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (_: Exception) { }
        }

        return openLiveUpdateSettings(context)
    }
}
