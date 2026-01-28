package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.monitors.NetworkMonitor
import com.example.phone_checker.data.monitors.NetworkState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StartNetworkMonitoringUseCase @Inject constructor(
    private val networkMonitor: NetworkMonitor
) {
    operator fun invoke() {
        networkMonitor.startMonitoring()
    }
}

class StopNetworkMonitoringUseCase @Inject constructor(
    private val networkMonitor: NetworkMonitor
) {
    operator fun invoke() {
        networkMonitor.stopMonitoring()
    }
}

class ObserveNetworkStateUseCase @Inject constructor(
    private val networkMonitor: NetworkMonitor
) {
    operator fun invoke(): Flow<NetworkState> = networkMonitor.networkState
}
