package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.repository.AppBehaviorInfo
import com.example.phone_checker.data.repository.AppBehaviorRepository
import com.example.phone_checker.data.repository.UsageInterval
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppBehaviorInfoUseCase @Inject constructor(
    private val appBehaviorRepository: AppBehaviorRepository
) {
    operator fun invoke(usageInterval: UsageInterval = UsageInterval.TODAY): Flow<AppBehaviorInfo> {
        return appBehaviorRepository.getAppBehaviorInfo(usageInterval)
    }
}
