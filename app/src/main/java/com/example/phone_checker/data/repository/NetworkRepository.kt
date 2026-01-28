package com.example.phone_checker.data.repository

import com.example.phone_checker.data.monitors.NetworkMonitor
import com.example.phone_checker.data.monitors.NetworkType as MonitorNetworkType
import com.example.phone_checker.data.monitors.WiFiFrequencyBand as MonitorWiFiFrequencyBand
import com.example.phone_checker.data.monitors.CellularGeneration as MonitorCellularGeneration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class NetworkInfo(
    val isConnected: Boolean,
    val networkType: NetworkType,
    val signalStrength: Int,
    val isMetered: Boolean,
    val hasInternet: Boolean,
    val wifiFrequencyBand: WiFiFrequencyBand?,
    val cellularGeneration: CellularGeneration?,
    val isVpnConnected: Boolean,
    val ssid: String?,
    val ipv4Address: String?,
    val ipv6Address: String?
)

enum class NetworkType {
    WIFI, CELLULAR, ETHERNET, BLUETOOTH, NONE
}

enum class WiFiFrequencyBand {
    BAND_2_4_GHZ, BAND_5_GHZ, BAND_6_GHZ, UNKNOWN
}

enum class CellularGeneration {
    GENERATION_2G, GENERATION_3G, GENERATION_4G, GENERATION_5G, UNKNOWN
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
            MonitorNetworkType.BLUETOOTH -> NetworkType.BLUETOOTH
            MonitorNetworkType.NONE, MonitorNetworkType.UNKNOWN -> NetworkType.NONE
        }
        
        val wifiFrequencyBand = if (state.networkType == MonitorNetworkType.WIFI) {
            when (state.wifiFrequencyBand) {
                MonitorWiFiFrequencyBand.BAND_2_4_GHZ -> WiFiFrequencyBand.BAND_2_4_GHZ
                MonitorWiFiFrequencyBand.BAND_5_GHZ -> WiFiFrequencyBand.BAND_5_GHZ
                MonitorWiFiFrequencyBand.BAND_6_GHZ -> WiFiFrequencyBand.BAND_6_GHZ
                MonitorWiFiFrequencyBand.UNKNOWN -> WiFiFrequencyBand.UNKNOWN
            }
        } else {
            null
        }
        
        val cellularGeneration = if (state.networkType == MonitorNetworkType.CELLULAR) {
            when (state.cellularGeneration) {
                MonitorCellularGeneration.GENERATION_2G -> CellularGeneration.GENERATION_2G
                MonitorCellularGeneration.GENERATION_3G -> CellularGeneration.GENERATION_3G
                MonitorCellularGeneration.GENERATION_4G -> CellularGeneration.GENERATION_4G
                MonitorCellularGeneration.GENERATION_5G -> CellularGeneration.GENERATION_5G
                MonitorCellularGeneration.UNKNOWN -> CellularGeneration.UNKNOWN
            }
        } else {
            null
        }
        
        NetworkInfo(
            isConnected = state.isConnected,
            networkType = networkType,
            signalStrength = state.signalStrength,
            isMetered = state.isMetered,
            hasInternet = state.hasInternetCapability,
            wifiFrequencyBand = wifiFrequencyBand,
            cellularGeneration = cellularGeneration,
            isVpnConnected = state.isVpnConnected,
            ssid = state.ssid,
            ipv4Address = state.ipv4Address,
            ipv6Address = state.ipv6Address
        )
    }
}
