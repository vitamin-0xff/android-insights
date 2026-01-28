package com.example.phone_checker.data.monitors

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class NetworkState(
    val isConnected: Boolean = false,
    val networkType: NetworkType = NetworkType.NONE,
    val signalStrength: Int = 0, // -1 to 100
    val isMetered: Boolean = false,
    val hasInternetCapability: Boolean = false
) {
    companion object {
        val Unknown = NetworkState()
        val Disconnected = NetworkState(isConnected = false)
    }
}

enum class NetworkType {
    NONE, WIFI, CELLULAR, ETHERNET, BLUETOOTH, UNKNOWN
}

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceMonitor {

    companion object {
        private const val TAG = "NetworkMonitor"
    }

    private val _isMonitoring = MutableStateFlow(false)
    override val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _networkState = MutableStateFlow(NetworkState.Unknown)
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun startMonitoring() {
        if (_isMonitoring.value) {
            Log.d(TAG, "Network monitoring already active")
            return
        }

        try {
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d(TAG, "Network available: $network")
                    updateNetworkState()
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.d(TAG, "Network lost: $network")
                    updateNetworkState()
                }

                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                    super.onCapabilitiesChanged(network, capabilities)
                    Log.d(TAG, "Network capabilities changed")
                    updateNetworkState()
                }

                override fun onLinkPropertiesChanged(network: Network, linkProperties: android.net.LinkProperties) {
                    super.onLinkPropertiesChanged(network, linkProperties)
                    Log.d(TAG, "Network link properties changed")
                    updateNetworkState()
                }
            }

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            networkCallback?.let {
                connectivityManager.registerNetworkCallback(request, it)
                _isMonitoring.value = true
                Log.d(TAG, "Network monitoring started")

                // Get initial state
                updateNetworkState()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting network monitoring", e)
            _isMonitoring.value = false
        }
    }

    override fun stopMonitoring() {
        if (!_isMonitoring.value) {
            Log.d(TAG, "Network monitoring already stopped")
            return
        }

        try {
            networkCallback?.let {
                connectivityManager.unregisterNetworkCallback(it)
                Log.d(TAG, "Network callback unregistered")
            }
            _isMonitoring.value = false
            Log.d(TAG, "Network monitoring stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping network monitoring", e)
        }
    }

    override fun cleanup() {
        stopMonitoring()
        _networkState.value = NetworkState.Unknown
    }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    private fun updateNetworkState() {
        try {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

            if (activeNetwork == null || capabilities == null) {
                _networkState.value = NetworkState.Disconnected
                return
            }

            val networkType = when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> NetworkType.BLUETOOTH
                else -> NetworkType.UNKNOWN
            }

            val isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

            val signalStrength = when (networkType) {
                NetworkType.WIFI -> getWifiSignalStrength()
                NetworkType.CELLULAR -> getCellularSignalStrength()
                else -> 0
            }

            _networkState.value = NetworkState(
                isConnected = true,
                networkType = networkType,
                signalStrength = signalStrength,
                isMetered = isMetered,
                hasInternetCapability = hasInternet
            )

            Log.d(TAG, "Network state updated: type=$networkType, signal=$signalStrength%, metered=$isMetered")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating network state", e)
            _networkState.value = NetworkState.Unknown
        }
    }

    private fun getWifiSignalStrength(): Int {
        return try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? android.net.wifi.WifiManager
            val linkSpeed = wifiManager?.connectionInfo?.linkSpeed ?: 0
            // Estimate signal from link speed (0-100)
            ((linkSpeed / 65f) * 100).toInt().coerceIn(0, 100)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting WiFi signal strength", e)
            0
        }
    }

    private fun getCellularSignalStrength(): Int {
        return try {
            // Would require TelephonyManager with additional permissions
            // For now, return placeholder
            0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cellular signal strength", e)
            0
        }
    }
}
