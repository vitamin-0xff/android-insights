package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.repository.ThermalInfo
import com.example.phone_checker.data.repository.ThermalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetThermalInfoUseCase @Inject constructor(
    private val thermalRepository: ThermalRepository
) {
    operator fun invoke(): Flow<ThermalInfo> {
        return thermalRepository.getThermalInfo()
    }
}
