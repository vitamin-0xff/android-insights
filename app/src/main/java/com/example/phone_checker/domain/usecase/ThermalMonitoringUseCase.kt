package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.monitors.ThermalMonitor
import com.example.phone_checker.data.monitors.ThermalStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StartThermalMonitoringUseCase @Inject constructor(
    private val thermalMonitor: ThermalMonitor
) {
    operator fun invoke() {
        thermalMonitor.startMonitoring()
    }
}

class StopThermalMonitoringUseCase @Inject constructor(
    private val thermalMonitor: ThermalMonitor
) {
    operator fun invoke() {
        thermalMonitor.stopMonitoring()
    }
}

class ObserveThermalStatusUseCase @Inject constructor(
    private val thermalMonitor: ThermalMonitor
) {
    operator fun invoke(): Flow<ThermalStatus> = thermalMonitor.thermalStatus
}
