package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.repository.SensorsHealthInfo
import com.example.phone_checker.data.repository.SensorsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSensorsHealthInfoUseCase @Inject constructor(
    private val sensorsRepository: SensorsRepository
) {
    operator fun invoke(): Flow<SensorsHealthInfo> {
        return sensorsRepository.getSensorsHealthInfo()
    }
}
