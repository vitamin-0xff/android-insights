package com.example.phone_checker.data.repository

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

data class AppBehaviorInfo(
    val totalAppsInstalled: Int,
    val systemApps: Int,
    val userApps: Int,
    val runningApps: Int,
    val topDrainApps: List<AppDrain>,
    val status: AppBehaviorStatus
)

data class AppDrain(
    val appName: String,
    val packageName: String,
    val usageTimeMinutes: Int
)

enum class AppBehaviorStatus {
    GOOD, MODERATE, CONCERNING
}

interface AppBehaviorRepository {
    fun getAppBehaviorInfo(): Flow<AppBehaviorInfo>
}

@Singleton
class AppBehaviorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppBehaviorRepository {

    override fun getAppBehaviorInfo(): Flow<AppBehaviorInfo> = flow {
        val packageManager = context.packageManager
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        
        // Get all installed apps
        val installedApps = try {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList()
        }
        
        val totalApps = installedApps.size
        val systemApps = installedApps.count { (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0 }
        val userApps = totalApps - systemApps
        
        // Get running apps
        val runningApps = try {
            activityManager.runningAppProcesses?.size ?: 0
        } catch (e: Exception) {
            0
        }
        
        // Get app usage stats for today
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val topDrainApps = mutableListOf<AppDrain>()
        
        try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfDay = calendar.timeInMillis
            val endOfDay = System.currentTimeMillis()
            
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startOfDay,
                endOfDay
            )
            
            usageStats
                ?.filter { it.totalTimeInForeground > 0 }
                ?.sortedByDescending { it.totalTimeInForeground }
                ?.take(5)
                ?.forEach { stats ->
                    try {
                        val appInfo = packageManager.getApplicationInfo(stats.packageName, 0)
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val usageMinutes = (stats.totalTimeInForeground / 60000).toInt()
                        
                        if (usageMinutes > 0) {
                            topDrainApps.add(
                                AppDrain(
                                    appName = appName,
                                    packageName = stats.packageName,
                                    usageTimeMinutes = usageMinutes
                                )
                            )
                        }
                    } catch (e: Exception) {
                        // Skip if app not found
                    }
                }
        } catch (e: Exception) {
            // Usage stats not available
        }
        
        val status = when {
            runningApps > 50 -> AppBehaviorStatus.CONCERNING
            runningApps > 30 -> AppBehaviorStatus.MODERATE
            else -> AppBehaviorStatus.GOOD
        }
        
        emit(
            AppBehaviorInfo(
                totalAppsInstalled = totalApps,
                systemApps = systemApps,
                userApps = userApps,
                runningApps = runningApps,
                topDrainApps = topDrainApps,
                status = status
            )
        )
    }
}
