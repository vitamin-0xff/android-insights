package com.example.phone_checker.data.monitors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.phone_checker.data.repository.BatteryHealth
import com.example.phone_checker.data.repository.BatteryInfo
import com.example.phone_checker.data.repository.BatteryStatus
import com.example.phone_checker.data.repository.ChargingType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.Class.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceMonitor {

    companion object {
        private const val TAG = "BatteryMonitor"
    }

    private val _isMonitoring = MutableStateFlow(false)
    override val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _batteryInfo = MutableStateFlow<BatteryInfo?>(null)
    val batteryInfo: StateFlow<BatteryInfo?> = _batteryInfo.asStateFlow()

    private var batteryReceiver: BroadcastReceiver? = null

    override fun startMonitoring() {
        if (_isMonitoring.value) {
            Log.d(TAG, "Battery monitoring already active")
            return
        }

        try {
            batteryReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        Intent.ACTION_BATTERY_CHANGED -> {
                            updateBatteryInfo(intent)
                        }
                        Intent.ACTION_POWER_CONNECTED -> {
                            Log.d(TAG, "Power connected")
                            // Will be reflected in next battery changed broadcast
                        }
                        Intent.ACTION_POWER_DISCONNECTED -> {
                            Log.d(TAG, "Power disconnected")
                            // Will be reflected in next battery changed broadcast
                        }
                    }
                }
            }

            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }

            // ContextCompat.registerReceiver handles API level differences internally
            // For API 33+, it uses RECEIVER_EXPORTED by default (appropriate for battery intents)
            ContextCompat.registerReceiver(context, batteryReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED)
            _isMonitoring.value = true
            Log.d(TAG, "Battery monitoring started")

            // Get initial battery info
            val batteryChangedFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val initialBatteryIntent = context.registerReceiver(null, batteryChangedFilter)
            initialBatteryIntent?.let { updateBatteryInfo(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting battery monitoring", e)
            _isMonitoring.value = false
        }
    }

    override fun stopMonitoring() {
        if (!_isMonitoring.value) {
            Log.d(TAG, "Battery monitoring already stopped")
            return
        }

        try {
            batteryReceiver?.let {
                context.unregisterReceiver(it)
                Log.d(TAG, "Battery receiver unregistered")
            }
            _isMonitoring.value = false
            Log.d(TAG, "Battery monitoring stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping battery monitoring", e)
        }
    }

    override fun cleanup() {
        stopMonitoring()
        _batteryInfo.value = null
    }

    private fun updateBatteryInfo(intent: Intent) {
        try {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = if (scale > 0) (level * 100 / scale) else 0

            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10f
            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

            val health = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
                BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
                BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
                BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
                else -> BatteryHealth.UNKNOWN
            }

            val status = when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                BatteryManager.BATTERY_STATUS_CHARGING -> BatteryStatus.CHARGING
                BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryStatus.DISCHARGING
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryStatus.NOT_CHARGING
                BatteryManager.BATTERY_STATUS_FULL -> BatteryStatus.FULL
                else -> BatteryStatus.UNKNOWN
            }

            val isCharging = status == BatteryStatus.CHARGING || status == BatteryStatus.FULL
            val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val chargingType = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> ChargingType.AC
                BatteryManager.BATTERY_PLUGGED_USB -> ChargingType.USB
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargingType.WIRELESS
                else -> if (isCharging) ChargingType.UNKNOWN else ChargingType.NONE
            }

            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val capacityPercent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

            val chargeCounterUah = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            val chargeCounterMah = if (chargeCounterUah > 0) chargeCounterUah / 1000 else null

            val currentNowUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            val currentNowMa = if (currentNowUa != Int.MIN_VALUE) currentNowUa / 1000 else null

            val currentAverageUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
            val currentAverageMa = if (currentAverageUa != Int.MIN_VALUE) currentAverageUa / 1000 else null

            val energyCounterNwh = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
            val energyCounter = if (energyCounterNwh > 0) energyCounterNwh else null

            // Get max capacity info - use system files
            val maxCapacityMah = getMaxCapacityFromSystemFiles()
            val maxEnergyNwh = if (maxCapacityMah != null && voltage > 0) {
                try {
                    // Energy (nWh) = Capacity (mAh) × Voltage (mV)
                    (maxCapacityMah.toLong() * voltage).toLong()
                } catch (e: Exception) {
                    null
                }
            } else null

            // Time estimates for API 28+ (P)
            val timeToFull = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    val millis = batteryManager.computeChargeTimeRemaining()
                    if (millis > 0) (millis / 60000).toInt() else null
                } catch (e: Exception) {
                    null
                }
            } else null

            // Estimate health percentage from capacity
            val healthPercent = if (capacityPercent in 1..100) capacityPercent else null

            val cycleCountValue: Int? = null

            val batteryData = BatteryInfo(
                level = batteryPct,
                temperature = temperature,
                voltage = voltage,
                health = health,
                status = status,
                isCharging = isCharging,
                technology = technology,
                capacityPercent = capacityPercent,
                chargeCounterMah = chargeCounterMah,
                currentNowMa = currentNowMa,
                currentAverageMa = currentAverageMa,
                energyCounterNwh = energyCounter,
                cycleCount = cycleCountValue,
                chargingType = chargingType,
                timeToFullMinutes = timeToFull,
                timeToEmptyMinutes = null,
                healthPercent = healthPercent,
                maxCapacityMah = maxCapacityMah ?: getBatteryCapacity(context)?.toInt(),
                maxEnergyNwh = maxEnergyNwh
            )

            _batteryInfo.value = batteryData
            Log.d(TAG, "Battery info updated: level=$batteryPct%, temp=${temperature}C, max=${maxCapacityMah}mAh, status=$status")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating battery info", e)
        }
    }


    // Source - https://stackoverflow.com/a
    // Posted by Domenico
    // Retrieved 2026-01-29, License - CC BY-SA 3.0
    fun getBatteryCapacity(context: Context?): Double? {
        val mPowerProfile: Any?
        var batteryCapacity = 0.0
        val POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile"

        try {
            mPowerProfile = forName(POWER_PROFILE_CLASS)
                .getConstructor(Context::class.java)
                .newInstance(context)

            batteryCapacity = forName(POWER_PROFILE_CLASS)
                .getMethod("getBatteryCapacity")
                .invoke(mPowerProfile) as Double
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return batteryCapacity
    }

    private fun getMaxCapacityFromSystemFiles(): Int? {
        return try {
            val paths = listOf(
                "/sys/class/power_supply/battery/charge_full_design",
                "/sys/class/power_supply/battery/energy_full_design",
                "/sys/class/power_supply/battery/charge_full",
                "/sys/class/power_supply/bms/charge_full"
            )

            for (path in paths) {
                try {
                    val file = java.io.File(path)
                    if (file.exists()) {
                        val capacityStr = file.readText().trim()
                        val capacityUa = capacityStr.toLongOrNull() ?: continue
                        // Convert microAh to mAh
                        return (capacityUa / 1000).toInt()
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error reading max capacity from system files", e)
            null
        }
    }
}
