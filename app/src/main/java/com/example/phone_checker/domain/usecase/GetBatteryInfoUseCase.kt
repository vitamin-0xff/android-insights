package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.monitors.BatteryMonitor
import com.example.phone_checker.data.repository.BatteryInfo
import com.example.phone_checker.data.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class GetBatteryInfoUseCase @Inject constructor(
    private val batteryRepository: BatteryRepository,
    private val batteryMonitor: BatteryMonitor
) {
    operator fun invoke(): Flow<BatteryInfo> {
        return batteryRepository.getBatteryInfo()
            .onStart {
                if (!batteryMonitor.isMonitoring.value) {
                    batteryMonitor.startMonitoring()
                }
            }
    }
}
