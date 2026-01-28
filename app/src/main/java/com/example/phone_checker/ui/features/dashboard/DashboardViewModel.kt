package com.example.phone_checker.ui.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phone_checker.data.repository.BatteryInfo
import com.example.phone_checker.data.repository.StorageInfo
import com.example.phone_checker.data.repository.PerformanceInfo
import com.example.phone_checker.data.repository.NetworkInfo
import com.example.phone_checker.data.repository.ScreenHealthInfo
import com.example.phone_checker.data.repository.AppBehaviorInfo
import com.example.phone_checker.data.repository.SensorsHealthInfo
import com.example.phone_checker.data.repository.AudioHealthInfo
import com.example.phone_checker.domain.model.DeviceInsight
import com.example.phone_checker.domain.usecase.GetBatteryInfoUseCase
import com.example.phone_checker.domain.usecase.GetDeviceInsightsUseCase
import com.example.phone_checker.domain.usecase.GetNetworkInfoUseCase
import com.example.phone_checker.domain.usecase.GetPerformanceInfoUseCase
import com.example.phone_checker.domain.usecase.GetStorageInfoUseCase
import com.example.phone_checker.domain.usecase.GetScreenHealthInfoUseCase
import com.example.phone_checker.domain.usecase.GetAppBehaviorInfoUseCase
import com.example.phone_checker.domain.usecase.GetSensorsHealthInfoUseCase
import com.example.phone_checker.domain.usecase.GetAudioHealthInfoUseCase
import com.example.phone_checker.utils.combineNineFlows
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllDeviceMetrics(
    val battery: BatteryInfo,
    val storage: StorageInfo,
    val performance: PerformanceInfo,
    val network: NetworkInfo,
    val insights: List<DeviceInsight>,
    val screen: ScreenHealthInfo,
    val apps: AppBehaviorInfo,
    val sensors: SensorsHealthInfo,
    val audio: AudioHealthInfo
)

data class DashboardUiState(
    val batteryLevel: Int = 0,
    val batteryStatus: String = "Unknown",
    val temperature: Float = 0f,
    val storageUsage: Int = 0,
    val ramUsage: Int = 0,
    val networkType: String = "None",
    val networkQuality: String = "Unknown",
    val screenOnTimeMinutes: Int = 0,
    val appsRunning: Int = 0,
    val sensorsHealth: String = "Unknown",
    val audioHealth: String = "Unknown",
    val criticalIssues: Int = 0,
    val warnings: Int = 0,
    val overallHealth: String = "Unknown",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getBatteryInfoUseCase: GetBatteryInfoUseCase,
    private val getStorageInfoUseCase: GetStorageInfoUseCase,
    private val getPerformanceInfoUseCase: GetPerformanceInfoUseCase,
    private val getScreenHealthInfoUseCase: GetScreenHealthInfoUseCase,
    private val getAppBehaviorInfoUseCase: GetAppBehaviorInfoUseCase,
    private val getNetworkInfoUseCase: GetNetworkInfoUseCase,
    private val getSensorsHealthInfoUseCase: GetSensorsHealthInfoUseCase,
    private val getAudioHealthInfoUseCase: GetAudioHealthInfoUseCase,
    private val getDeviceInsightsUseCase: GetDeviceInsightsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDeviceMetrics()
    }

    private fun loadDeviceMetrics() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState(isLoading = true)
            
                combineNineFlows(
                    getBatteryInfoUseCase(),
                    getStorageInfoUseCase(),
                    getPerformanceInfoUseCase(),
                    getNetworkInfoUseCase(),
                    getDeviceInsightsUseCase(),
                    getScreenHealthInfoUseCase(),
                    getAppBehaviorInfoUseCase(),
                    getSensorsHealthInfoUseCase(),
                    getAudioHealthInfoUseCase()

                    ) { f1, f2, f3, f4, f5, f6, f7, f8, f9 ->
                    AllDeviceMetrics(
                            battery = f1,
                            storage = f2,
                            performance = f3,
                            network = f4,
                            insights = f5,
                            screen = f6,
                            apps = f7,
                            sensors = f8,
                            audio = f9
                        )
                }

                    .collect { metrics ->
                        val critical = metrics.insights.count { 
                            it.severity == com.example.phone_checker.domain.model.InsightSeverity.CRITICAL 
                        }
                        val warnings = metrics.insights.count { 
                            it.severity == com.example.phone_checker.domain.model.InsightSeverity.WARNING 
                        }
                        
                        val overallHealth = when {
                            critical > 0 -> "Critical"
                            warnings > 0 -> "Needs Attention"
                            else -> "Good"
                        }
                        _uiState.value = DashboardUiState(
                            batteryLevel = metrics.battery.level,
                            batteryStatus = if (metrics.battery.isCharging) "Charging" else "On Battery",
                            temperature = metrics.battery.temperature,
                            storageUsage = metrics.storage.usagePercentage,
                            ramUsage = metrics.performance.ramUsagePercent,
                            networkType = metrics.network.networkType.name,
                            networkQuality = metrics.network.connectionQuality.name,
                            screenOnTimeMinutes = metrics.screen.screenOnTimeMinutes,
                            appsRunning = metrics.apps.runningApps,
                            sensorsHealth = metrics.sensors.status.name,
                            criticalIssues = critical,
                            warnings = warnings,
                            overallHealth = overallHealth
                        )
                    }
        }
    }

    fun refresh() {
        loadDeviceMetrics()
    }
}
