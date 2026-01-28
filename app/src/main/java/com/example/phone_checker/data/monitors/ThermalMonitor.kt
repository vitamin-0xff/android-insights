package com.example.phone_checker.data.monitors

import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class ThermalStatus {
    NONE, LIGHT, MODERATE, SEVERE, CRITICAL, SHUTDOWN, EMERGENCY, UNKNOWN, NOT_SUPPORTED
}

@Singleton
class ThermalMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceMonitor {

    companion object {
        private const val TAG = "ThermalMonitor"
    }

    private val _isMonitoring = MutableStateFlow(false)
    override val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _thermalStatus = MutableStateFlow(ThermalStatus.UNKNOWN)
    val thermalStatus: StateFlow<ThermalStatus> = _thermalStatus.asStateFlow()

    private var thermalListener: PowerManager.OnThermalStatusChangedListener? = null

    override fun startMonitoring() {
        if (_isMonitoring.value) {
            Log.d(TAG, "Thermal monitoring already active")
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Log.w(TAG, "Thermal monitoring requires Android 10 (API 29+)")
            _thermalStatus.value = ThermalStatus.NOT_SUPPORTED
            _isMonitoring.value = false
            return
        }

        try {
            registerThermalListener()
            _isMonitoring.value = true
            Log.d(TAG, "Thermal monitoring started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting thermal monitoring", e)
            _isMonitoring.value = false
        }
    }

    override fun stopMonitoring() {
        if (!_isMonitoring.value) {
            Log.d(TAG, "Thermal monitoring already stopped")
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                unregisterThermalListener()
            }
            _isMonitoring.value = false
            Log.d(TAG, "Thermal monitoring stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping thermal monitoring", e)
        }
    }

    override fun cleanup() {
        stopMonitoring()
        _thermalStatus.value = ThermalStatus.UNKNOWN
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun registerThermalListener() {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

            thermalListener = PowerManager.OnThermalStatusChangedListener { status ->
                val thermalStatus = when (status) {
                    PowerManager.THERMAL_STATUS_NONE -> ThermalStatus.NONE
                    PowerManager.THERMAL_STATUS_LIGHT -> ThermalStatus.LIGHT
                    PowerManager.THERMAL_STATUS_MODERATE -> ThermalStatus.MODERATE
                    PowerManager.THERMAL_STATUS_SEVERE -> ThermalStatus.SEVERE
                    PowerManager.THERMAL_STATUS_CRITICAL -> ThermalStatus.CRITICAL
                    PowerManager.THERMAL_STATUS_EMERGENCY -> ThermalStatus.EMERGENCY
                    PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalStatus.SHUTDOWN
                    else -> ThermalStatus.UNKNOWN
                }
                _thermalStatus.value = thermalStatus
                Log.d(TAG, "Thermal status changed: $thermalStatus")
            }

            thermalListener?.let {
                powerManager.addThermalStatusListener(it)
            }

            // Get initial status
            val currentStatus = powerManager.currentThermalStatus
            val initialStatus = when (currentStatus) {
                PowerManager.THERMAL_STATUS_NONE -> ThermalStatus.NONE
                PowerManager.THERMAL_STATUS_LIGHT -> ThermalStatus.LIGHT
                PowerManager.THERMAL_STATUS_MODERATE -> ThermalStatus.MODERATE
                PowerManager.THERMAL_STATUS_SEVERE -> ThermalStatus.SEVERE
                PowerManager.THERMAL_STATUS_CRITICAL -> ThermalStatus.CRITICAL
                PowerManager.THERMAL_STATUS_EMERGENCY -> ThermalStatus.EMERGENCY
                PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalStatus.SHUTDOWN
                else -> ThermalStatus.UNKNOWN
            }
            _thermalStatus.value = initialStatus
            Log.d(TAG, "Initial thermal status: $initialStatus")
        } catch (e: Exception) {
            Log.e(TAG, "Error registering thermal listener", e)
            _thermalStatus.value = ThermalStatus.NOT_SUPPORTED
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun unregisterThermalListener() {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            thermalListener?.let {
                powerManager.removeThermalStatusListener(it)
                Log.d(TAG, "Thermal listener unregistered")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering thermal listener", e)
        }
    }
}
