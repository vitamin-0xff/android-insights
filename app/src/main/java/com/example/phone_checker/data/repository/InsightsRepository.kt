package com.example.phone_checker.data.repository

import com.example.phone_checker.domain.model.DeviceInsight
import com.example.phone_checker.domain.model.InsightCategory
import com.example.phone_checker.domain.model.InsightSeverity
import com.example.phone_checker.utils.combineNineFlows
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface InsightsRepository {
    fun getDeviceInsights(): Flow<List<DeviceInsight>>
}

data class AllRepositoryData(
    val battery: BatteryInfo,
    val thermal: ThermalInfo,
    val storage: StorageInfo,
    val performance: PerformanceInfo,
    val network: NetworkInfo,
    val screen: ScreenHealthInfo,
    val apps: AppBehaviorInfo,
    val sensors: SensorsHealthInfo,
    val audio: AudioHealthInfo
)

@Singleton
class InsightsRepositoryImpl @Inject constructor(
    private val batteryRepository: BatteryRepository,
    private val thermalRepository: ThermalRepository,
    private val storageRepository: StorageRepository,
    private val performanceRepository: PerformanceRepository,
    private val networkRepository: NetworkRepository,
    private val screenHealthRepository: ScreenHealthRepository,
    private val appBehaviorRepository: AppBehaviorRepository,
    private val sensorsRepository: SensorsRepository,
    private val audioRepository: AudioRepository
) : InsightsRepository {

    override fun getDeviceInsights(): Flow<List<DeviceInsight>> {
        // this approach is not scalable but it's a way to get the job done!
        return combineNineFlows(
            batteryRepository.getBatteryInfo(),
            thermalRepository.getThermalInfo(),
            storageRepository.getStorageInfo(),
            performanceRepository.getPerformanceInfo(),
            networkRepository.getNetworkInfo(),
            screenHealthRepository.getScreenHealthInfo(),
            appBehaviorRepository.getAppBehaviorInfo(),
            sensorsRepository.getSensorsHealthInfo(),
            audioRepository.getAudioHealthInfo()
        ) { f1, f2, f3, f4, f5, f6, f7, f8, f9 ->
            buildInsights(
                AllRepositoryData(
                    battery = f1,
                    thermal = f2,
                    storage = f3,
                    performance = f4,
                    network = f5,
                    screen = f6,
                    apps = f7,
                    sensors = f8,
                    audio = f9
                )
            )
        }
    }

    private fun buildInsights(data: AllRepositoryData): List<DeviceInsight> {
        val battery = data.battery
        val thermal = data.thermal
        val storage = data.storage
        val performance = data.performance
        val network = data.network
        val screen = data.screen
        val apps = data.apps
        val sensors = data.sensors
        val audio = data.audio

        return buildList {
            // Battery Insights
            when {
                battery.level < 20 -> add(
                    DeviceInsight(
                        id = "battery_low",
                        category = InsightCategory.BATTERY,
                        severity = InsightSeverity.WARNING,
                        title = "Low Battery",
                        description = "Your battery is at ${battery.level}%",
                        recommendation = "Charge your device soon to avoid unexpected shutdown. Consider enabling battery saver mode."
                    )
                )

                battery.level < 50 && !battery.isCharging -> add(
                    DeviceInsight(
                        id = "battery_moderate",
                        category = InsightCategory.BATTERY,
                        severity = InsightSeverity.INFO,
                        title = "Battery Moderate",
                        description = "Battery at ${battery.level}%",
                        recommendation = "Plan to charge your device within the next few hours."
                    )
                )

                battery.level >= 80 && battery.isCharging -> add(
                    DeviceInsight(
                        id = "battery_optimal",
                        category = InsightCategory.BATTERY,
                        severity = InsightSeverity.POSITIVE,
                        title = "Battery Healthy",
                        description = "Battery is at ${battery.level}% and charging",
                        recommendation = "Your battery is in good condition. Consider unplugging when fully charged to extend battery lifespan."
                    )
                )
            }

            // Battery Health Insights
            when (battery.health) {
                BatteryHealth.OVERHEAT -> add(
                    DeviceInsight(
                        id = "battery_overheat",
                        category = InsightCategory.BATTERY,
                        severity = InsightSeverity.CRITICAL,
                        title = "Battery Overheating",
                        description = "Battery health indicates overheating",
                        recommendation = "Stop using the device immediately. Remove from charger and let it cool down in a well-ventilated area."
                    )
                )

                BatteryHealth.DEAD, BatteryHealth.OVER_VOLTAGE -> add(
                    DeviceInsight(
                        id = "battery_health_critical",
                        category = InsightCategory.BATTERY,
                        severity = InsightSeverity.CRITICAL,
                        title = "Battery Health Critical",
                        description = "Battery health is compromised",
                        recommendation = "Consider replacing your battery. Visit an authorized service center for inspection."
                    )
                )

                BatteryHealth.COLD -> add(
                    DeviceInsight(
                        id = "battery_cold",
                        category = InsightCategory.BATTERY,
                        severity = InsightSeverity.WARNING,
                        title = "Battery Too Cold",
                        description = "Battery temperature is too low",
                        recommendation = "Warm up the device gradually. Avoid using it in very cold environments."
                    )
                )

                else -> {}
            }

            // Thermal Insights
            when (thermal.status) {
                ThermalStatus.HOT -> add(
                    DeviceInsight(
                        id = "thermal_hot",
                        category = InsightCategory.THERMAL,
                        severity = InsightSeverity.WARNING,
                        title = "Device Running Hot",
                        description = "Temperature is ${thermal.batteryTemperature}°C",
                        recommendation = "Close background apps, reduce screen brightness, and avoid intensive tasks. Let the device cool down."
                    )
                )

                ThermalStatus.CRITICAL -> add(
                    DeviceInsight(
                        id = "thermal_critical",
                        category = InsightCategory.THERMAL,
                        severity = InsightSeverity.CRITICAL,
                        title = "Critical Temperature",
                        description = "Temperature is dangerously high at ${thermal.batteryTemperature}°C",
                        recommendation = "STOP using the device immediately! Power it off and let it cool down. Seek technical support if this persists."
                    )
                )

                ThermalStatus.WARM -> add(
                    DeviceInsight(
                        id = "thermal_warm",
                        category = InsightCategory.THERMAL,
                        severity = InsightSeverity.INFO,
                        title = "Device Warming Up",
                        description = "Temperature is ${thermal.batteryTemperature}°C",
                        recommendation = "Monitor your device. Consider reducing intensive tasks if temperature continues to rise."
                    )
                )

                else -> {}
            }

            // Storage Insights
            when (storage.status) {
                StorageStatus.CRITICAL -> add(
                    DeviceInsight(
                        id = "storage_critical",
                        category = InsightCategory.STORAGE,
                        severity = InsightSeverity.CRITICAL,
                        title = "Storage Almost Full",
                        description = "Storage is ${storage.usagePercentage}% full",
                        recommendation = "Delete unused apps, clear cache, and remove old photos/videos immediately. Low storage can cause system instability."
                    )
                )

                StorageStatus.WARNING -> add(
                    DeviceInsight(
                        id = "storage_warning",
                        category = InsightCategory.STORAGE,
                        severity = InsightSeverity.WARNING,
                        title = "Storage Getting Full",
                        description = "Storage is ${storage.usagePercentage}% full",
                        recommendation = "Consider freeing up space by removing unused apps, clearing app cache, or transferring files to cloud storage."
                    )
                )

                else -> {}
            }

            // Combined Insights
            if (battery.temperature > 40f && thermal.status >= ThermalStatus.HOT) {
                add(
                    DeviceInsight(
                        id = "combined_thermal",
                        category = InsightCategory.SYSTEM,
                        severity = InsightSeverity.CRITICAL,
                        title = "System Overheating",
                        description = "Both battery and device temperatures are elevated",
                        recommendation = "Critical: Stop all activities, close all apps, and let the device cool down completely before use."
                    )
                )
            }

            if (battery.level < 20 && !battery.isCharging && storage.status == StorageStatus.CRITICAL) {
                add(
                    DeviceInsight(
                        id = "combined_critical",
                        category = InsightCategory.SYSTEM,
                        severity = InsightSeverity.CRITICAL,
                        title = "Multiple Critical Issues",
                        description = "Low battery and critical storage detected",
                        recommendation = "Charge your device and free up storage space immediately to prevent data loss and system issues."
                    )
                )
            }

            // Performance Insights
            when (performance.status) {
                PerformanceStatus.POOR -> add(
                    DeviceInsight(
                        id = "performance_poor",
                        category = InsightCategory.PERFORMANCE,
                        severity = InsightSeverity.WARNING,
                        title = "Performance Degraded",
                        description = "RAM usage at ${performance.ramUsagePercent}%, CPU at ${performance.cpuUsagePercent.toInt()}%",
                        recommendation = "Close unused apps to free up memory. Restart your device if performance issues persist."
                    )
                )

                PerformanceStatus.MODERATE -> add(
                    DeviceInsight(
                        id = "performance_moderate",
                        category = InsightCategory.PERFORMANCE,
                        severity = InsightSeverity.INFO,
                        title = "Moderate System Load",
                        description = "RAM at ${performance.ramUsagePercent}%, ${performance.availableRamMb}MB available",
                        recommendation = "Consider closing background apps to improve responsiveness."
                    )
                )

                else -> {}
            }

            // Network Insights
            when {
                !network.isConnected -> add(
                    DeviceInsight(
                        id = "network_disconnected",
                        category = InsightCategory.SYSTEM,
                        severity = InsightSeverity.WARNING,
                        title = "No Network Connection",
                        description = "Device is not connected to any network",
                        recommendation = "Connect to WiFi or enable mobile data to access online services."
                    )
                )

                network.signalStrength < 40 -> add(
                    DeviceInsight(
                        id = "network_poor",
                        category = InsightCategory.SYSTEM,
                        severity = InsightSeverity.INFO,
                        title = "Weak Network Signal",
                        description = "${network.networkType.name} signal is weak (${network.signalStrength}%)",
                        recommendation = "Move closer to your router or switch to a stronger network for better connectivity."
                    )
                )

                else -> {}
            }

            // Combined Insights - Performance + Battery
            if (performance.ramUsagePercent > 85 && battery.level < 30) {
                add(
                    DeviceInsight(
                        id = "combined_performance_battery",
                        category = InsightCategory.SYSTEM,
                        severity = InsightSeverity.CRITICAL,
                        title = "System Under Stress",
                        description = "High memory usage combined with low battery",
                        recommendation = "Close all non-essential apps immediately and charge your device."
                    )
                )
            }

            // Positive insights
            if (battery.level > 70 && thermal.status == ThermalStatus.NORMAL && storage.status == StorageStatus.HEALTHY) {
                add(
                    DeviceInsight(
                        id = "system_healthy",
                        category = InsightCategory.SYSTEM,
                        severity = InsightSeverity.POSITIVE,
                        title = "Device Health Excellent",
                        description = "All systems operating optimally",
                        recommendation = "Your device is in great condition. Continue current usage patterns for optimal performance."
                    )
                )
            }

            // Screen Display Insights
            if (screen.isHdr) {
                add(
                    DeviceInsight(
                        id = "screen_hdr",
                        category = InsightCategory.SYSTEM,
                        severity = InsightSeverity.POSITIVE,
                        title = "HDR Display Supported",
                        description = "HDR capabilities detected on ${screen.displayName}",
                        recommendation = "Enjoy HDR content for richer colors and contrast."
                    )
                )
            }

            if (screen.isWideColorGamut) {
                add(
                    DeviceInsight(
                        id = "screen_wide_color",
                        category = InsightCategory.SYSTEM,
                        severity = InsightSeverity.POSITIVE,
                        title = "Wide Color Gamut",
                        description = "Wide color gamut support detected",
                        recommendation = "Use compatible apps to take advantage of expanded colors."
                    )
                )
            }

            if (screen.refreshRate >= 90f) {
                add(
                    DeviceInsight(
                        id = "screen_high_refresh",
                        category = InsightCategory.SYSTEM,
                        severity = InsightSeverity.POSITIVE,
                        title = "High Refresh Rate",
                        description = "Display running at ${screen.refreshRate.toInt()}Hz",
                        recommendation = "Smooth visuals enabled. Lower refresh rates can save battery."
                    )
                )
            }

            // App Behavior Insights
            when (apps.status) {
                AppBehaviorStatus.CONCERNING -> add(
                    DeviceInsight(
                        id = "apps_concerning",
                        category = InsightCategory.PERFORMANCE,
                        severity = InsightSeverity.WARNING,
                        title = "Too Many Running Apps",
                        description = "${apps.runningApps} apps are currently running",
                        recommendation = "Close unused apps to improve performance and battery life."
                    )
                )

                AppBehaviorStatus.MODERATE -> add(
                    DeviceInsight(
                        id = "apps_moderate",
                        category = InsightCategory.PERFORMANCE,
                        severity = InsightSeverity.INFO,
                        title = "Many Apps Running",
                        description = "${apps.runningApps} active processes detected",
                        recommendation = "Consider closing background apps you're not using."
                    )
                )

                else -> {}
            }

            // Heavy app usage insights
            if (apps.topDrainApps.isNotEmpty()) {
                val topApp = apps.topDrainApps.first()
                if (topApp.usageTimeMinutes > 120) {
                    add(
                        DeviceInsight(
                            id = "app_heavy_usage",
                            category = InsightCategory.BATTERY,
                            severity = InsightSeverity.INFO,
                            title = "High App Usage Detected",
                            description = "${topApp.appName} used for ${topApp.usageTimeMinutes / 60}h ${topApp.usageTimeMinutes % 60}m",
                            recommendation = "This app may be contributing to battery drain. Monitor its usage."
                        )
                    )
                }
            }



            // Storage + Apps insight
            if (storage.usagePercentage > 85 && apps.userApps > 100) {
                add(
                    DeviceInsight(
                        id = "combined_storage_apps",
                        category = InsightCategory.STORAGE,
                        severity = InsightSeverity.WARNING,
                        title = "Too Many Apps Installed",
                        description = "${apps.userApps} user apps with ${storage.usagePercentage}% storage used",
                        recommendation = "Uninstall apps you don't use to free up storage space."
                    )
                )
            }
            // Sensors Insights
            when (sensors.status) {
                SensorsHealthStatus.POOR -> add(
                    DeviceInsight(
                        id = "sensors_poor",
                        category = InsightCategory.BATTERY,
                        severity = InsightSeverity.WARNING,
                        title = "Sensor Health Degraded",
                        description = "${sensors.activeSensors}/${sensors.totalSensors} sensors active",
                        recommendation = "Check device settings for sensor issues."
                    )
                )
                else -> {}
            }
        }
    }
}
