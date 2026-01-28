package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.repository.BatteryInfo
import com.example.phone_checker.data.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBatteryInfoUseCase @Inject constructor(
    private val batteryRepository: BatteryRepository
) {
    operator fun invoke(): Flow<BatteryInfo> {
        return batteryRepository.getBatteryInfo()
    }
}
