package com.example.phone_checker.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    @ApplicationContext private val context: Context
) : NetworkRepository {

    override fun getNetworkInfo(): Flow<NetworkInfo> = flow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }
        
        val isConnected = capabilities != null
        
        val networkType = when {
            capabilities == null -> NetworkType.NONE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            else -> NetworkType.NONE
        }
        
        var wifiSignalStrength: Int? = null
        var wifiLinkSpeed: Int? = null
        var networkName: String? = null
        
        if (networkType == NetworkType.WIFI) {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            
            // Calculate signal strength percentage (RSSI ranges from -100 to -50 typically)
            val rssi = wifiInfo.rssi
            wifiSignalStrength = when {
                rssi >= -50 -> 100
                rssi <= -100 -> 0
                else -> 2 * (rssi + 100)
            }
            
            wifiLinkSpeed = wifiInfo.linkSpeed
            networkName = wifiInfo.ssid?.removeSurrounding("\"")
        }
        
        var cellularSignalStrength: Int? = null
        
        if (networkType == NetworkType.CELLULAR) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) 
                == PackageManager.PERMISSION_GRANTED) {
                try {
                    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    networkName = telephonyManager.networkOperatorName
                    // Signal strength would require more complex API handling
                    cellularSignalStrength = 75 // Placeholder
                } catch (e: Exception) {
                    // Handle permission or API issues
                }
            }
        }
        
        val quality = when {
            !isConnected -> ConnectionQuality.DISCONNECTED
            networkType == NetworkType.WIFI && (wifiSignalStrength ?: 0) >= 80 -> ConnectionQuality.EXCELLENT
            networkType == NetworkType.WIFI && (wifiSignalStrength ?: 0) >= 60 -> ConnectionQuality.GOOD
            networkType == NetworkType.WIFI && (wifiSignalStrength ?: 0) >= 40 -> ConnectionQuality.FAIR
            networkType == NetworkType.WIFI -> ConnectionQuality.POOR
            networkType == NetworkType.CELLULAR -> ConnectionQuality.GOOD
            networkType == NetworkType.ETHERNET -> ConnectionQuality.EXCELLENT
            else -> ConnectionQuality.DISCONNECTED
        }
        
        emit(
            NetworkInfo(
                isConnected = isConnected,
                networkType = networkType,
                connectionQuality = quality,
                wifiSignalStrength = wifiSignalStrength,
                wifiLinkSpeed = wifiLinkSpeed,
                cellularSignalStrength = cellularSignalStrength,
                networkName = networkName
            )
        )
    }
}
