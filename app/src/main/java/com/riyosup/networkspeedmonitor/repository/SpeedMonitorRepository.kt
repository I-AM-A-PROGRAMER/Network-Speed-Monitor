package com.riyosup.networkspeedmonitor.repository

import com.riyosup.networkspeedmonitor.model.SpeedData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared in-memory state repository for the network monitoring session.
 * Exposes observable StateFlows for UI consumption without IPC overhead.
 */
object SpeedMonitorRepository {

    private val _isMonitoringActive = MutableStateFlow(false)
    val isMonitoringActive: StateFlow<Boolean> = _isMonitoringActive.asStateFlow()

    private val _currentSpeed = MutableStateFlow(SpeedData())
    val currentSpeed: StateFlow<SpeedData> = _currentSpeed.asStateFlow()

    fun setMonitoringActive(active: Boolean) {
        _isMonitoringActive.value = active
        if (!active) {
            _currentSpeed.value = SpeedData()
        }
    }

    fun updateSpeed(speed: SpeedData) {
        _currentSpeed.value = speed
    }
}
