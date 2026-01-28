package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.repository.PerformanceInfo
import com.example.phone_checker.data.repository.PerformanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPerformanceInfoUseCase @Inject constructor(
    private val performanceRepository: PerformanceRepository
) {
    operator fun invoke(): Flow<PerformanceInfo> {
        return performanceRepository.getPerformanceInfo()
    }
}
