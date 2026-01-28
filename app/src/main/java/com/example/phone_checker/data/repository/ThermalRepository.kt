package com.example.phone_checker.data.repository

import com.example.phone_checker.data.monitors.BatteryMonitor
import com.example.phone_checker.data.monitors.ThermalMonitor
import com.example.phone_checker.data.monitors.ThermalStatus as MonitorThermalStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class ThermalInfo(
    val batteryTemperature: Float,
    val status: ThermalStatus,
    val recommendation: String
)

enum class ThermalStatus {
    NORMAL, WARM, HOT, CRITICAL
}

interface ThermalRepository {
    fun getThermalInfo(): Flow<ThermalInfo>
}

@Singleton
class ThermalRepositoryImpl @Inject constructor(
    private val batteryMonitor: BatteryMonitor,
    private val thermalMonitor: ThermalMonitor
) : ThermalRepository {

    override fun getThermalInfo(): Flow<ThermalInfo> = combine(
        batteryMonitor.batteryInfo.filterNotNull(),
        thermalMonitor.thermalStatus
    ) { batteryInfo, thermalStatus ->
        val temperature = batteryInfo.temperature
        
        // Use battery temperature to determine status
        val status = when {
            temperature < 35f -> ThermalStatus.NORMAL
            temperature < 40f -> ThermalStatus.WARM
            temperature < 45f -> ThermalStatus.HOT
            else -> ThermalStatus.CRITICAL
        }
        
        val recommendation = when (status) {
            ThermalStatus.NORMAL -> "Device temperature is optimal"
            ThermalStatus.WARM -> "Device is getting warm. Consider reducing usage."
            ThermalStatus.HOT -> "Device is hot! Close background apps and let it cool down."
            ThermalStatus.CRITICAL -> "Critical temperature! Stop using the device immediately and let it cool."
        }

        ThermalInfo(
            batteryTemperature = temperature,
            status = status,
            recommendation = recommendation
        )
    }
}
