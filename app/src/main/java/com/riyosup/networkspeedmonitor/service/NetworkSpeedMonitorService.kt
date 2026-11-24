package com.riyosup.networkspeedmonitor.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.riyosup.networkspeedmonitor.model.SpeedCalculator
import com.riyosup.networkspeedmonitor.model.SpeedData
import com.riyosup.networkspeedmonitor.notification.NotificationHelper
import com.riyosup.networkspeedmonitor.repository.SpeedMonitorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Dedicated foreground service that periodically measures device-wide network throughput
 * and posts live metric updates to the ongoing notification.
 */
class NetworkSpeedMonitorService : Service() {

    companion object {
        const val ACTION_START = "com.riyosup.networkspeedmonitor.ACTION_START"
        const val ACTION_STOP = "com.riyosup.networkspeedmonitor.ACTION_STOP"

        fun startService(context: Context) {
            val intent = Intent(context, NetworkSpeedMonitorService::class.java).apply {
                action = ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, NetworkSpeedMonitorService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var monitoringJob: Job? = null
    private val speedCalculator = SpeedCalculator()
    private lateinit var notificationHelper: NotificationHelper
    private var isMonitoringRunning = false

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopMonitoring()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                startMonitoring()
            }
            else -> {
                // If restarted by system with START_STICKY
                if (SpeedMonitorRepository.isMonitoringActive.value) {
                    startMonitoring()
                } else {
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
        }
        return START_STICKY
    }

    @Synchronized
    private fun startMonitoring() {
        if (isMonitoringRunning) return
        isMonitoringRunning = true

        val initialSpeed = SpeedData()
        val initialNotification = notificationHelper.buildNotification(initialSpeed)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NotificationHelper.NOTIFICATION_ID,
                    initialNotification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NotificationHelper.NOTIFICATION_ID, initialNotification)
            }
        } catch (e: Exception) {
            // Guard against potential ForegroundServiceStartNotAllowedException on newer Android
            isMonitoringRunning = false
            SpeedMonitorRepository.setMonitoringActive(false)
            stopSelf()
            return
        }

        SpeedMonitorRepository.setMonitoringActive(true)

        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            speedCalculator.reset()
            while (isActive) {
                val speed = speedCalculator.calculateSpeed()
                SpeedMonitorRepository.updateSpeed(speed)
                notificationHelper.updateNotification(speed)
                delay(1000L)
            }
        }
    }

    @Synchronized
    private fun stopMonitoring() {
        if (!isMonitoringRunning) {
            stopSelf()
            return
        }
        stopMonitoringInternal()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationHelper.cancelNotification()
        stopSelf()
    }

    private fun stopMonitoringInternal() {
        isMonitoringRunning = false
        monitoringJob?.cancel()
        monitoringJob = null
        speedCalculator.reset()
        SpeedMonitorRepository.setMonitoringActive(false)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Keep foreground service active when app is swiped away from Recents
    }

    override fun onDestroy() {
        stopMonitoringInternal()
        serviceScope.cancel()
        super.onDestroy()
    }
}
