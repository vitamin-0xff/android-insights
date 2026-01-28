package com.example.phone_checker.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.provider.Settings
import com.example.phone_checker.data.monitors.DisplayMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

data class ScreenHealthInfo(
    val screenOnTimeMinutes: Int,
    val avgBrightnessLevel: Int, // 0-255
    val autoBrightnessEnabled: Boolean,
    val screenTimeout: Int, // seconds
    val status: ScreenHealthStatus,
    val recommendation: String
)

enum class ScreenHealthStatus {
    OPTIMAL, MODERATE, EXCESSIVE
}

interface ScreenHealthRepository {
    fun getScreenHealthInfo(): Flow<ScreenHealthInfo>
}

@Singleton
class ScreenHealthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val displayMonitor: DisplayMonitor
) : ScreenHealthRepository {

    override fun getScreenHealthInfo(): Flow<ScreenHealthInfo> = combine(
        flow { emit(calculateScreenOnTime()) },
        displayMonitor.displayState
    ) { screenOnTimeMinutes, displayState ->
        
        // Get brightness settings
        val brightnessMode = try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE
            )
        } catch (e: Exception) {
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        }
        
        val autoBrightnessEnabled = brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        
        val brightness = try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
        } catch (e: Exception) {
            128
        }
        
        val screenTimeout = try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT
            ) / 1000
        } catch (e: Exception) {
            30
        }
        
        val status = when {
            screenOnTimeMinutes < 180 -> ScreenHealthStatus.OPTIMAL
            screenOnTimeMinutes < 360 -> ScreenHealthStatus.MODERATE
            else -> ScreenHealthStatus.EXCESSIVE
        }
        
        val recommendation = when {
            screenOnTimeMinutes > 360 -> "Screen time is very high (${screenOnTimeMinutes / 60}h ${screenOnTimeMinutes % 60}m today)."
            !autoBrightnessEnabled && brightness > 200 -> "Enable auto-brightness to save battery and reduce eye strain."
            else -> "Screen usage is healthy. ${screenOnTimeMinutes / 60}h ${screenOnTimeMinutes % 60}m today."
        }
        
        ScreenHealthInfo(
            screenOnTimeMinutes = screenOnTimeMinutes,
            avgBrightnessLevel = brightness,
            autoBrightnessEnabled = autoBrightnessEnabled,
            screenTimeout = screenTimeout,
            status = status,
            recommendation = recommendation
        )
    }
    
    private fun calculateScreenOnTime(): Int {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.timeInMillis
        val endOfDay = System.currentTimeMillis()
        
        var screenOnTimeMinutes = 0
        
        try {
            val usageEvents = usageStatsManager.queryEvents(startOfDay, endOfDay)
            var lastScreenOn = 0L
            
            while (usageEvents.hasNextEvent()) {
                val event = android.app.usage.UsageEvents.Event()
                usageEvents.getNextEvent(event)
                
                when (event.eventType) {
                    android.app.usage.UsageEvents.Event.SCREEN_INTERACTIVE -> {
                        lastScreenOn = event.timeStamp
                    }
                    android.app.usage.UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                        if (lastScreenOn > 0) {
                            screenOnTimeMinutes += ((event.timeStamp - lastScreenOn) / 60000).toInt()
                            lastScreenOn = 0
                        }
                    }
                }
            }
            
            if (lastScreenOn > 0) {
                screenOnTimeMinutes += ((endOfDay - lastScreenOn) / 60000).toInt()
            }
        } catch (e: Exception) {
            screenOnTimeMinutes = 120
        }
        
        return screenOnTimeMinutes
    }
}
