package com.example.phone_checker.data.repository

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.RandomAccessFile
import javax.inject.Inject
import javax.inject.Singleton

data class PerformanceInfo(
    val cpuUsagePercent: Float,
    val totalRamMb: Long,
    val usedRamMb: Long,
    val availableRamMb: Long,
    val ramUsagePercent: Int,
    val appMemoryUsageMb: Float,
    val status: PerformanceStatus
)

enum class PerformanceStatus {
    EXCELLENT, GOOD, MODERATE, POOR
}

interface PerformanceRepository {
    fun getPerformanceInfo(): Flow<PerformanceInfo>
}

@Singleton
class PerformanceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PerformanceRepository {

    override fun getPerformanceInfo(): Flow<PerformanceInfo> = flow {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        
        // Get RAM info
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val totalRamMb = memoryInfo.totalMem / (1024 * 1024)
        val availableRamMb = memoryInfo.availMem / (1024 * 1024)
        val usedRamMb = totalRamMb - availableRamMb
        val ramUsagePercent = ((usedRamMb.toFloat() / totalRamMb.toFloat()) * 100).toInt()
        
        // Get app memory usage
        val runtime = Runtime.getRuntime()
        val appMemoryUsageMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f)
        
        // Get CPU usage (simplified)
        val cpuUsage = getCpuUsage()
        
        // Determine status
        val status = when {
            ramUsagePercent < 50 && cpuUsage < 30 -> PerformanceStatus.EXCELLENT
            ramUsagePercent < 70 && cpuUsage < 50 -> PerformanceStatus.GOOD
            ramUsagePercent < 85 && cpuUsage < 70 -> PerformanceStatus.MODERATE
            else -> PerformanceStatus.POOR
        }
        
        emit(
            PerformanceInfo(
                cpuUsagePercent = cpuUsage,
                totalRamMb = totalRamMb,
                usedRamMb = usedRamMb,
                availableRamMb = availableRamMb,
                ramUsagePercent = ramUsagePercent,
                appMemoryUsageMb = appMemoryUsageMb,
                status = status
            )
        )
    }
    
    private fun getCpuUsage(): Float {
        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val load = reader.readLine()
            reader.close()
            
            val toks = load.split(" +".toRegex())
            
            val idle = toks[4].toLong()
            val cpu = toks.slice(1..4).sumOf { it.toLong() }
            
            val usage = ((cpu - idle).toFloat() / cpu.toFloat()) * 100
            usage.coerceIn(0f, 100f)
        } catch (e: Exception) {
            0f
        }
    }
}
