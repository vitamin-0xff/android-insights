package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.monitors.NetworkMonitor
import com.example.phone_checker.data.repository.NetworkInfo
import com.example.phone_checker.data.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class GetNetworkInfoUseCase @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val networkMonitor: NetworkMonitor
) {
    operator fun invoke(): Flow<NetworkInfo> {
        return networkRepository.getNetworkInfo()
            .onStart {
                if (!networkMonitor.isMonitoring.value) {
                    networkMonitor.startMonitoring()
                }
            }
    }
}
