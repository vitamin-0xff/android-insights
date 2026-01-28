package com.example.phone_checker.data.monitors

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

enum class MonitorType {
    BATTERY, NETWORK, THERMAL, AUDIO, DISPLAY
}

@Singleton
class MonitorManager @Inject constructor(
    private val batteryMonitor: BatteryMonitor,
    private val networkMonitor: NetworkMonitor,
    private val thermalMonitor: ThermalMonitor,
    private val audioMonitor: AudioMonitor,
    private val displayMonitor: DisplayMonitor
) {

    companion object {
        private const val TAG = "MonitorManager"
    }

    private val allMonitors = mapOf(
        MonitorType.BATTERY to batteryMonitor,
        MonitorType.NETWORK to networkMonitor,
        MonitorType.THERMAL to thermalMonitor,
        MonitorType.AUDIO to audioMonitor,
        MonitorType.DISPLAY to displayMonitor
    )

    fun startAllMonitors() {
        Log.d(TAG, "Starting all monitors")
        allMonitors.values.forEach { monitor ->
            try {
                monitor.startMonitoring()
            } catch (e: Exception) {
                Log.e(TAG, "Error starting monitor: ${monitor.javaClass.simpleName}", e)
            }
        }
    }

    fun stopAllMonitors() {
        Log.d(TAG, "Stopping all monitors")
        allMonitors.values.forEach { monitor ->
            try {
                monitor.stopMonitoring()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping monitor: ${monitor.javaClass.simpleName}", e)
            }
        }
    }

    fun cleanupAllMonitors() {
        Log.d(TAG, "Cleaning up all monitors")
        allMonitors.values.forEach { monitor ->
            try {
                monitor.cleanup()
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up monitor: ${monitor.javaClass.simpleName}", e)
            }
        }
    }

    fun startSelectiveMonitoring(monitorTypes: Set<MonitorType>) {
        Log.d(TAG, "Starting selective monitoring for: $monitorTypes")
        monitorTypes.forEach { type ->
            val monitor = allMonitors[type]
            if (monitor != null) {
                try {
                    monitor.startMonitoring()
                    Log.d(TAG, "Started ${type.name} monitor")
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting ${type.name} monitor", e)
                }
            }
        }
    }

    fun stopSelectiveMonitoring(monitorTypes: Set<MonitorType>) {
        Log.d(TAG, "Stopping selective monitoring for: $monitorTypes")
        monitorTypes.forEach { type ->
            val monitor = allMonitors[type]
            if (monitor != null) {
                try {
                    monitor.stopMonitoring()
                    Log.d(TAG, "Stopped ${type.name} monitor")
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping ${type.name} monitor", e)
                }
            }
        }
    }

    fun getBatteryMonitor(): BatteryMonitor = batteryMonitor

    fun getNetworkMonitor(): NetworkMonitor = networkMonitor

    fun getThermalMonitor(): ThermalMonitor = thermalMonitor

    fun getAudioMonitor(): AudioMonitor = audioMonitor

    fun getDisplayMonitor(): DisplayMonitor = displayMonitor
}
