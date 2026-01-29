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
    val capacityPercent: Int,
    val chargeCounterMah: Int?,
    val currentNowMa: Int?,
    val currentAverageMa: Int?,
    val energyCounterNwh: Long?,
    val cycleCount: Int?,
    val chargingType: ChargingType,
    val timeToFullMinutes: Int?,
    val timeToEmptyMinutes: Int?,
    val healthPercent: Int?,
    val maxCapacityMah: Int?,
    val maxEnergyNwh: Long?
)

enum class BatteryHealth {
    GOOD, OVERHEAT, DEAD, OVER_VOLTAGE, COLD, UNKNOWN
}

enum class BatteryStatus {
    CHARGING, DISCHARGING, NOT_CHARGING, FULL, UNKNOWN
}
    
enum class ChargingType {
    AC, USB, WIRELESS, DOCK, NONE, UNKNOWN
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
