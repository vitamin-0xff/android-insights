package com.example.phone_checker.data.monitors

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.LinkProperties
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import java.net.Inet4Address
import java.net.Inet6Address

data class NetworkState(
    val isConnected: Boolean = false,
    val networkType: NetworkType = NetworkType.NONE,
    val signalStrength: Int = 0,
    val isMetered: Boolean = false,
    val hasInternetCapability: Boolean = false,
    val wifiFrequencyBand: WiFiFrequencyBand = WiFiFrequencyBand.UNKNOWN,
    val cellularGeneration: CellularGeneration = CellularGeneration.UNKNOWN,
    val isVpnConnected: Boolean = false,
    val ssid: String? = null,
    val ipv4Address: String? = null,
    val ipv6Address: String? = null
) {
    companion object {
        val Unknown = NetworkState()
        val Disconnected = NetworkState(isConnected = false)
    }
}

enum class NetworkType {
    NONE, WIFI, CELLULAR, ETHERNET, BLUETOOTH, UNKNOWN
}

enum class WiFiFrequencyBand {
    BAND_2_4_GHZ, BAND_5_GHZ, BAND_6_GHZ, UNKNOWN
}

enum class CellularGeneration {
    GENERATION_2G, GENERATION_3G, GENERATION_4G, GENERATION_5G, UNKNOWN
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
            val linkProperties = connectivityManager.getLinkProperties(activeNetwork)

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
            val isVpn = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)

            val signalStrength = when (networkType) {
                NetworkType.WIFI -> getWifiSignalStrength()
                NetworkType.CELLULAR -> getCellularSignalStrength()
                else -> 0
            }

            val wifiFrequencyBand = if (networkType == NetworkType.WIFI) {
                getWifiFrequencyBand()
            } else {
                WiFiFrequencyBand.UNKNOWN
            }

            val cellularGeneration = if (networkType == NetworkType.CELLULAR) {
                getCellularGeneration()
            } else {
                CellularGeneration.UNKNOWN
            }

            val ssid = if (networkType == NetworkType.WIFI) {
                getWifiSsid()
            } else {
                null
            }

            val (ipv4, ipv6) = getIpAddresses(linkProperties)

            _networkState.value = NetworkState(
                isConnected = true,
                networkType = networkType,
                signalStrength = signalStrength,
                isMetered = isMetered,
                hasInternetCapability = hasInternet,
                wifiFrequencyBand = wifiFrequencyBand,
                cellularGeneration = cellularGeneration,
                isVpnConnected = isVpn,
                ssid = ssid,
                ipv4Address = ipv4,
                ipv6Address = ipv6
            )

            Log.d(TAG, "Network state updated: type=$networkType, signal=$signalStrength%, ssid=$ssid, ipv4=$ipv4, ipv6=$ipv6")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating network state", e)
            _networkState.value = NetworkState.Unknown
        }
    }

    private fun getWifiSignalStrength(): Int {
        return try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? android.net.wifi.WifiManager
            @Suppress("DEPRECATION")
            val linkSpeed = wifiManager?.connectionInfo?.linkSpeed ?: 0
            ((linkSpeed / 65f) * 100).toInt().coerceIn(0, 100)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting WiFi signal strength", e)
            0
        }
    }

    private fun getWifiFrequencyBand(): WiFiFrequencyBand {
        return try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? android.net.wifi.WifiManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                @Suppress("DEPRECATION")
                val connectionInfo = wifiManager?.connectionInfo
                val frequency = connectionInfo?.frequency ?: return WiFiFrequencyBand.UNKNOWN
                
                when {
                    frequency in 2400..2500 -> WiFiFrequencyBand.BAND_2_4_GHZ
                    frequency in 5000..6000 -> WiFiFrequencyBand.BAND_5_GHZ
                    frequency in 6000..7000 -> WiFiFrequencyBand.BAND_6_GHZ
                    else -> WiFiFrequencyBand.UNKNOWN
                }
            } else {
                @Suppress("DEPRECATION")
                val connectionInfo = wifiManager?.connectionInfo
                val frequency = connectionInfo?.frequency ?: return WiFiFrequencyBand.UNKNOWN
                
                when {
                    frequency in 2400..2500 -> WiFiFrequencyBand.BAND_2_4_GHZ
                    frequency in 5000..6000 -> WiFiFrequencyBand.BAND_5_GHZ
                    else -> WiFiFrequencyBand.UNKNOWN
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting WiFi frequency band", e)
            WiFiFrequencyBand.UNKNOWN
        }
    }

    private fun getCellularSignalStrength(): Int {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val signalStrength = telephonyManager?.signalStrength
                signalStrength?.level?.let { (it * 25) } ?: 0
            } else {
                0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cellular signal strength", e)
            0
        }
    }

    @Suppress("DEPRECATION")
    private fun getCellularGeneration(): CellularGeneration {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            when (telephonyManager?.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN -> CellularGeneration.GENERATION_2G
                
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP -> CellularGeneration.GENERATION_3G
                
                TelephonyManager.NETWORK_TYPE_LTE -> CellularGeneration.GENERATION_4G
                
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (telephonyManager?.dataNetworkType == TelephonyManager.NETWORK_TYPE_NR) {
                            CellularGeneration.GENERATION_5G
                        } else {
                            CellularGeneration.UNKNOWN
                        }
                    } else {
                        CellularGeneration.UNKNOWN
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cellular generation", e)
            CellularGeneration.UNKNOWN
        }
    }

    private fun getWifiSsid(): String? {
        return try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? android.net.wifi.WifiManager
            @Suppress("DEPRECATION")
            val connectionInfo = wifiManager?.connectionInfo
            val ssid = connectionInfo?.ssid
            // Remove quotes if present
            ssid?.removeSurrounding("\"")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting WiFi SSID", e)
            null
        }
    }

    private fun getIpAddresses(linkProperties: android.net.LinkProperties?): Pair<String?, String?> {
        return try {
            var ipv4Address: String? = null
            var ipv6Address: String? = null

            linkProperties?.linkAddresses?.forEach { linkAddress ->
                val address = linkAddress.address
                when (address) {
                    is Inet4Address -> {
                        if (ipv4Address == null) {
                            ipv4Address = address.hostAddress
                        }
                    }
                    is Inet6Address -> {
                        if (ipv6Address == null && !address.isLinkLocalAddress) {
                            ipv6Address = address.hostAddress?.split("%")?.getOrNull(0)
                        }
                    }
                }
            }

            Pair(ipv4Address, ipv6Address)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP addresses", e)
            Pair(null, null)
        }
    }
}