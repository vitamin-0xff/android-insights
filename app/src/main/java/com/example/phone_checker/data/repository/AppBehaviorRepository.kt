package com.example.phone_checker.data.repository

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
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
    val launchableApps: Int,
    val disabledApps: Int,
    val recentlyUpdatedApps: Int,
    val totalMemoryUsageMb: Long,
    val topDrainApps: List<AppDrain>,
    val topMemoryApps: List<AppMemoryInfo>,
    val recentUpdates: List<AppUpdateInfo>,
    val usageInterval: UsageInterval,
    val status: AppBehaviorStatus
)

data class AppDrain(
    val appName: String,
    val packageName: String,
    val usageTimeMinutes: Int
)

data class AppMemoryInfo(
    val appName: String,
    val packageName: String,
    val memoryUsageMb: Long
)

data class AppUpdateInfo(
    val appName: String,
    val packageName: String,
    val updateDaysAgo: Int
)

enum class UsageInterval(val displayName: String, val days: Int) {
    TODAY("Today", 0),
    LAST_7_DAYS("Last 7 Days", 7),
    LAST_30_DAYS("Last 30 Days", 30),
    LAST_90_DAYS("Last 90 Days", 90)
}

enum class AppBehaviorStatus {
    GOOD, MODERATE, CONCERNING
}

interface AppBehaviorRepository {
    fun getAppBehaviorInfo(usageInterval: UsageInterval = UsageInterval.TODAY): Flow<AppBehaviorInfo>
}

@Singleton
class AppBehaviorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppBehaviorRepository {

    override fun getAppBehaviorInfo(usageInterval: UsageInterval): Flow<AppBehaviorInfo> = flow {
        val packageManager = context.packageManager
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        
        // Get all installed apps
        val installedApps = try {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList()
        }
        
        val totalApps = installedApps.size
        
        // Count user apps: apps that are NOT system apps, OR are updated system apps
        // User apps = apps installed by user + updated system apps
        val userApps = installedApps.count { appInfo ->
            // Check if it's a user-installed app (not a system app)
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            
            // User app if: not a system app OR is an updated system app
            !isSystemApp || isUpdatedSystemApp
        }
        
        val systemApps = totalApps - userApps
        
        // Count launchable apps (apps with launcher activity)
        val launchableApps = try {
            val mainIntent = android.content.Intent(android.content.Intent.ACTION_MAIN, null)
            mainIntent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            packageManager.queryIntentActivities(mainIntent, 0).size
        } catch (e: Exception) {
            0
        }
        
        // Count disabled apps
        val disabledApps = installedApps.count { appInfo ->
            !appInfo.enabled
        }
        
        // Get running apps
        val runningApps = try {
            activityManager.runningAppProcesses?.size ?: 0
        } catch (e: Exception) {
            0
        }
        
        // Get memory usage information
        val topMemoryApps = mutableListOf<AppMemoryInfo>()
        var totalMemoryUsage = 0L
        
        try {
            val runningProcesses = activityManager.runningAppProcesses ?: emptyList()
            val pids = runningProcesses.map { it.pid }.toIntArray()
            
            if (pids.isNotEmpty()) {
                val memoryInfos = activityManager.getProcessMemoryInfo(pids)
                
                runningProcesses.forEachIndexed { index, processInfo ->
                    if (index < memoryInfos.size) {
                        val memInfo = memoryInfos[index]
                        val memoryKb = memInfo.totalPss
                        val memoryMb = (memoryKb / 1024).toLong()
                        
                        totalMemoryUsage += memoryMb
                        
                        if (memoryMb > 10) { // Only track apps using more than 10MB
                            try {
                                val appInfo = packageManager.getApplicationInfo(processInfo.processName, 0)
                                val appName = packageManager.getApplicationLabel(appInfo).toString()
                                
                                topMemoryApps.add(
                                    AppMemoryInfo(
                                        appName = appName,
                                        packageName = processInfo.processName,
                                        memoryUsageMb = memoryMb
                                    )
                                )
                            } catch (e: Exception) {
                                // Skip if package not found
                            }
                        }
                    }
                }
            }
            
            // Sort by memory usage and take top 5
            topMemoryApps.sortByDescending { it.memoryUsageMb }
            while (topMemoryApps.size > 5) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    topMemoryApps.removeLast()
                }
            }
        } catch (e: Exception) {
            // Memory info not available
        }
        
        // Get recently updated apps (last 7 days)
        val recentUpdates = mutableListOf<AppUpdateInfo>()
        val currentTime = System.currentTimeMillis()
        val sevenDaysAgo = currentTime - (7 * 24 * 60 * 60 * 1000)
        
        try {
            installedApps.forEach { appInfo ->
                try {
                    val packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0)
                    val updateTime = packageInfo.lastUpdateTime
                    
                    if (updateTime > sevenDaysAgo) {
                        val daysAgo = ((currentTime - updateTime) / (24 * 60 * 60 * 1000)).toInt()
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        
                        recentUpdates.add(
                            AppUpdateInfo(
                                appName = appName,
                                packageName = appInfo.packageName,
                                updateDaysAgo = daysAgo
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Skip if package not found
                }
            }
            
            // Sort by most recent and limit to 10
            recentUpdates.sortBy { it.updateDaysAgo }
            while (recentUpdates.size > 10) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    recentUpdates.removeLast()
                }
            }
        } catch (e: Exception) {
            // Update info not available
        }
        
        val recentlyUpdatedCount = recentUpdates.size
        
        // Get app usage stats for selected interval
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val topDrainApps = mutableListOf<AppDrain>()
        
        try {
            val calendar = Calendar.getInstance()
            val endTime = System.currentTimeMillis()
            val startTime = when (usageInterval) {
                UsageInterval.TODAY -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.timeInMillis
                }
                else -> {
                    endTime - (usageInterval.days * 24 * 60 * 60 * 1000L)
                }
            }
            
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
            
            // Group by package name and sum up usage times for multi-day periods
            val aggregatedUsage = mutableMapOf<String, Long>()
            usageStats?.forEach { stats ->
                if (stats.totalTimeInForeground > 0) {
                    val currentTotal = aggregatedUsage[stats.packageName] ?: 0L
                    aggregatedUsage[stats.packageName] = currentTotal + stats.totalTimeInForeground
                }
            }
            
            // Sort by total usage and take top 5
            aggregatedUsage
                .entries
                .sortedByDescending { it.value }
                .take(5)
                .forEach { (packageName, totalTime) ->
                    try {
                        val appInfo = packageManager.getApplicationInfo(packageName, 0)
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val usageMinutes = (totalTime / 60000).toInt()
                        
                        if (usageMinutes > 0) {
                            topDrainApps.add(
                                AppDrain(
                                    appName = appName,
                                    packageName = packageName,
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
                usageInterval = usageInterval,
                launchableApps = launchableApps,
                disabledApps = disabledApps,
                recentlyUpdatedApps = recentlyUpdatedCount,
                totalMemoryUsageMb = totalMemoryUsage,
                topDrainApps = topDrainApps,
                topMemoryApps = topMemoryApps,
                recentUpdates = recentUpdates,
                status = status
            )
        )
    }
}
