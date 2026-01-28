package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.repository.NetworkInfo
import com.example.phone_checker.data.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNetworkInfoUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    operator fun invoke(): Flow<NetworkInfo> {
        return networkRepository.getNetworkInfo()
    }
}
