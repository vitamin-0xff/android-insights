package com.example.phone_checker.data.repository

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Debug
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.RandomAccessFile
import javax.inject.Inject
import javax.inject.Singleton

data class PerformanceInfo(
    val cpuUsagePercent: Float,
    val cpuCores: Int,
    val cpuMaxFrequencyMhz: Int?,
    val cpuCurrentFrequencyMhz: Int?,
    val totalRamMb: Long,
    val usedRamMb: Long,
    val availableRamMb: Long,
    val ramUsagePercent: Int,
    val appMemoryUsageMb: Float,
    val nativeHeapMb: Float,
    val dalvikHeapMb: Float,
    val threadCount: Int,
    val appProcesses: Int,
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
        
        // Get native and Dalvik heap info
        val nativeHeapMb = Debug.getNativeHeapAllocatedSize() / (1024f * 1024f)
        val dalvikHeapMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f)
        
        // Get thread count
        val threadCount = Thread.activeCount()
        
        // Get CPU info
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val cpuMaxFreq = getCpuMaxFrequency()
        val cpuCurrentFreq = getCpuCurrentFrequency()
        
        // Get CPU usage (needs two snapshots)
        val cpuUsage = getCpuUsage()
        
        // Get app processes count
        val appProcesses = activityManager.runningAppProcesses?.size ?: 0
        
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
                cpuCores = cpuCores,
                cpuMaxFrequencyMhz = cpuMaxFreq,
                cpuCurrentFrequencyMhz = cpuCurrentFreq,
                totalRamMb = totalRamMb,
                usedRamMb = usedRamMb,
                availableRamMb = availableRamMb,
                ramUsagePercent = ramUsagePercent,
                appMemoryUsageMb = appMemoryUsageMb,
                nativeHeapMb = nativeHeapMb,
                dalvikHeapMb = dalvikHeapMb,
                threadCount = threadCount,
                appProcesses = appProcesses,
                status = status
            )
        )
    }
    
    private suspend fun getCpuUsage(): Float {
        return try {
            // Take first snapshot
            val firstSnapshot = readCpuStats()
            
            // Wait 500ms
            delay(500)
            
            // Take second snapshot
            val secondSnapshot = readCpuStats()
            
            if (firstSnapshot != null && secondSnapshot != null) {
                val totalDelta = secondSnapshot.total - firstSnapshot.total
                val idleDelta = secondSnapshot.idle - firstSnapshot.idle
                
                if (totalDelta > 0) {
                    val usage = (1.0f - (idleDelta.toFloat() / totalDelta.toFloat())) * 100f
                    usage.coerceIn(0f, 100f)
                } else {
                    0f
                }
            } else {
                0f
            }
        } catch (e: Exception) {
            0f
        }
    }
    
    private data class CpuStats(val total: Long, val idle: Long)
    
    private fun readCpuStats(): CpuStats? {
        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val load = reader.readLine()
            reader.close()
            
            val toks = load.split(" +".toRegex()).drop(1) // Skip "cpu" label
            
            if (toks.size >= 5) {
                val user = toks[0].toLong()
                val nice = toks[1].toLong()
                val system = toks[2].toLong()
                val idle = toks[3].toLong()
                val iowait = toks[4].toLong()
                
                val total = user + nice + system + idle + iowait
                CpuStats(total, idle)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getCpuMaxFrequency(): Int? {
        return try {
            val cpuCores = Runtime.getRuntime().availableProcessors()
            var maxFreq = 0
            
            for (i in 0 until cpuCores) {
                val freqFile = File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
                if (freqFile.exists()) {
                    val freq = freqFile.readText().trim().toIntOrNull()
                    if (freq != null && freq > maxFreq) {
                        maxFreq = freq
                    }
                }
            }
            
            if (maxFreq > 0) maxFreq / 1000 else null // Convert to MHz
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getCpuCurrentFrequency(): Int? {
        return try {
            val cpuCores = Runtime.getRuntime().availableProcessors()
            var totalFreq = 0L
            var count = 0
            
            for (i in 0 until cpuCores) {
                val freqFile = File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
                if (freqFile.exists()) {
                    val freq = freqFile.readText().trim().toLongOrNull()
                    if (freq != null && freq > 0) {
                        totalFreq += freq
                        count++
                    }
                }
            }
            
            if (count > 0) (totalFreq / count / 1000).toInt() else null // Average MHz
        } catch (e: Exception) {
            null
        }
    }
}
