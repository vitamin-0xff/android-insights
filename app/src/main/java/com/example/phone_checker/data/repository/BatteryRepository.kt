package com.example.phone_checker.data.repository

import com.example.phone_checker.data.monitors.BatteryMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
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
    private val batteryMonitor: BatteryMonitor
) : BatteryRepository {

    override fun getBatteryInfo(): Flow<BatteryInfo> = 
        batteryMonitor.batteryInfo.filterNotNull()
}
