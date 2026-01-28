package com.example.phone_checker.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

data class BatteryInfo(
    val level: Int,
    val temperature: Float,
    val voltage: Int,
    val health: BatteryHealth,
    val status: BatteryStatus,
    val isCharging: Boolean,
    val technology: String,
    val capacityPercent: Int, // Current capacity as percentage of design capacity
    val chargeCounterMah: Int?, // Current charge in mAh
    val currentNowMa: Int?, // Current flow in mA (positive = charging, negative = discharging)
    val currentAverageMa: Int?, // Average current in mA
    val energyCounterNwh: Long?, // Remaining energy in nWh
    val cycleCount: Int? // Number of charge cycles (if available)
)

enum class BatteryHealth {
    GOOD, OVERHEAT, DEAD, OVER_VOLTAGE, COLD, UNKNOWN
}

enum class BatteryStatus {
    CHARGING, DISCHARGING, NOT_CHARGING, FULL, UNKNOWN
}

interface BatteryRepository {
    fun getBatteryInfo(): Flow<BatteryInfo>
}

@Singleton
class BatteryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BatteryRepository {

    override fun getBatteryInfo(): Flow<BatteryInfo> = flow {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }

        batteryStatus?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = level * 100 / scale

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

            // Get BatteryManager for advanced metrics
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            
            // Capacity percentage (current capacity / design capacity)
            val capacityPercent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            
            // Charge counter in microampere-hours (convert to mAh)
            val chargeCounterUah = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            val chargeCounterMah = if (chargeCounterUah > 0) chargeCounterUah / 1000 else null
            
            // Current flow in microamperes (convert to mA)
            val currentNowUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            val currentNowMa = if (currentNowUa != Int.MIN_VALUE) currentNowUa / 1000 else null
            
            // Average current in microamperes (convert to mA)
            val currentAverageUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
            val currentAverageMa = if (currentAverageUa != Int.MIN_VALUE) currentAverageUa / 1000 else null
            
            // Energy counter in nanowatt-hours
            val energyCounterNwh = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
            val energyCounter = if (energyCounterNwh > 0) energyCounterNwh else null
            
            // Cycle count (Android 14+, may not be available on all devices)
            val cycleCount = try {
                // This property might not be available on all devices
                val cycleCountValue = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                if (cycleCountValue > 0 && cycleCountValue < 10000) cycleCountValue else null
            } catch (e: Exception) {
                null
            }

            emit(
                BatteryInfo(
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
                    cycleCount = cycleCount
                )
            )
        }
    }
}
