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
    @ApplicationContext private val context: Context
) : ThermalRepository {

    override fun getThermalInfo(): Flow<ThermalInfo> = flow {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }

        batteryStatus?.let { intent ->
            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10f
            
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

            emit(
                ThermalInfo(
                    batteryTemperature = temperature,
                    status = status,
                    recommendation = recommendation
                )
            )
        }
    }
}
