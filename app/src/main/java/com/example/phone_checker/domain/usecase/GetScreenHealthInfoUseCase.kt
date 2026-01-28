package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.repository.ScreenHealthInfo
import com.example.phone_checker.data.repository.ScreenHealthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetScreenHealthInfoUseCase @Inject constructor(
    private val screenHealthRepository: ScreenHealthRepository
) {
    operator fun invoke(): Flow<ScreenHealthInfo> {
        return screenHealthRepository.getScreenHealthInfo()
    }
}
