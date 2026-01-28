package com.example.phone_checker.data.repository

import com.example.phone_checker.data.monitors.NetworkMonitor
import com.example.phone_checker.data.monitors.NetworkType as MonitorNetworkType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class NetworkInfo(
    val isConnected: Boolean,
    val networkType: NetworkType,
    val connectionQuality: ConnectionQuality,
    val wifiSignalStrength: Int?, // 0-100%
    val wifiLinkSpeed: Int?, // Mbps
    val cellularSignalStrength: Int?, // 0-100%
    val networkName: String?
)

enum class NetworkType {
    WIFI, CELLULAR, ETHERNET, NONE
}

enum class ConnectionQuality {
    EXCELLENT, GOOD, FAIR, POOR, DISCONNECTED
}

interface NetworkRepository {
    fun getNetworkInfo(): Flow<NetworkInfo>
}

@Singleton
class NetworkRepositoryImpl @Inject constructor(
    private val networkMonitor: NetworkMonitor
) : NetworkRepository {

    override fun getNetworkInfo(): Flow<NetworkInfo> = networkMonitor.networkState.map { state ->
        val networkType = when (state.networkType) {
            MonitorNetworkType.WIFI -> NetworkType.WIFI
            MonitorNetworkType.CELLULAR -> NetworkType.CELLULAR
            MonitorNetworkType.ETHERNET -> NetworkType.ETHERNET
            MonitorNetworkType.BLUETOOTH -> NetworkType.NONE // Map Bluetooth to NONE as repository doesn't support it
            MonitorNetworkType.NONE, MonitorNetworkType.UNKNOWN -> NetworkType.NONE
        }
        
        val quality = when {
            !state.isConnected -> ConnectionQuality.DISCONNECTED
            state.signalStrength >= 80 -> ConnectionQuality.EXCELLENT
            state.signalStrength >= 60 -> ConnectionQuality.GOOD
            state.signalStrength >= 40 -> ConnectionQuality.FAIR
            else -> ConnectionQuality.POOR
        }
        
        NetworkInfo(
            isConnected = state.isConnected,
            networkType = networkType,
            connectionQuality = quality,
            wifiSignalStrength = if (networkType == NetworkType.WIFI) state.signalStrength else null,
            wifiLinkSpeed = null, // Can be added if needed
            cellularSignalStrength = if (networkType == NetworkType.CELLULAR) state.signalStrength else null,
            networkName = null // Can be added if needed
        )
    }
}
