package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.repository.StorageInfo
import com.example.phone_checker.data.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStorageInfoUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(): Flow<StorageInfo> {
        return storageRepository.getStorageInfo()
    }
}
