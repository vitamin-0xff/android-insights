package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.monitors.BatteryMonitor
import com.example.phone_checker.data.repository.BatteryInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

class StartBatteryMonitoringUseCase @Inject constructor(
    private val batteryMonitor: BatteryMonitor
) {
    operator fun invoke() {
        batteryMonitor.startMonitoring()
    }
}

class StopBatteryMonitoringUseCase @Inject constructor(
    private val batteryMonitor: BatteryMonitor
) {
    operator fun invoke() {
        batteryMonitor.stopMonitoring()
    }
}

class ObserveBatteryInfoUseCase @Inject constructor(
    private val batteryMonitor: BatteryMonitor
) {
    operator fun invoke(): Flow<BatteryInfo> = batteryMonitor.batteryInfo.filterNotNull()
}
