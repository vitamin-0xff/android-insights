package com.example.phone_checker.data.repository

import android.content.Context
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

data class StorageInfo(
    val totalSpace: Long,
    val usedSpace: Long,
    val freeSpace: Long,
    val usagePercentage: Int,
    val status: StorageStatus
)

enum class StorageStatus {
    HEALTHY, WARNING, CRITICAL
}

interface StorageRepository {
    fun getStorageInfo(): Flow<StorageInfo>
}

@Singleton
class StorageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StorageRepository {

    override fun getStorageInfo(): Flow<StorageInfo> = flow {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalSpace = totalBlocks * blockSize
        val freeSpace = availableBlocks * blockSize
        val usedSpace = totalSpace - freeSpace
        val usagePercentage = ((usedSpace.toDouble() / totalSpace.toDouble()) * 100).toInt()

        val status = when {
            usagePercentage < 70 -> StorageStatus.HEALTHY
            usagePercentage < 90 -> StorageStatus.WARNING
            else -> StorageStatus.CRITICAL
        }

        emit(
            StorageInfo(
                totalSpace = totalSpace,
                usedSpace = usedSpace,
                freeSpace = freeSpace,
                usagePercentage = usagePercentage,
                status = status
            )
        )
    }
}
