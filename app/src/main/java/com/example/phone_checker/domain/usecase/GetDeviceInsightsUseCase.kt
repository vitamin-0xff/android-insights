package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.repository.InsightsRepository
import com.example.phone_checker.domain.model.DeviceInsight
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDeviceInsightsUseCase @Inject constructor(
    private val insightsRepository: InsightsRepository
) {
    operator fun invoke(): Flow<List<DeviceInsight>> {
        return insightsRepository.getDeviceInsights()
    }
}
