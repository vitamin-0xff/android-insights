package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.repository.AudioHealthInfo
import com.example.phone_checker.data.repository.AudioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAudioHealthInfoUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    operator fun invoke(): Flow<AudioHealthInfo> {
        return audioRepository.getAudioHealthInfo()
    }
}
