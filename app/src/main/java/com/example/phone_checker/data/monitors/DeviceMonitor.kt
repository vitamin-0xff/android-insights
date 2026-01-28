package com.example.phone_checker.data.monitors

import kotlinx.coroutines.flow.StateFlow

/**
 * Base interface for all device monitors that provide real-time data through listeners/callbacks.
 * Monitors are responsible for managing lifecycle of Android system listeners and exposing
 * collected data through Kotlin Flow/StateFlow for reactive consumption.
 */
interface DeviceMonitor {
    val isMonitoring: StateFlow<Boolean>

    fun startMonitoring()

    fun stopMonitoring()

    fun cleanup()
}
