package com.example.phone_checker.ui.features.network

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.phone_checker.data.repository.CellularGeneration
import com.example.phone_checker.data.repository.NetworkInfo
import com.example.phone_checker.data.repository.NetworkType
import com.example.phone_checker.data.repository.WiFiFrequencyBand
import com.example.phone_checker.ui.theme.PhonecheckerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    onNavigateBack: () -> Unit,
    viewModel: NetworkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            uiState.networkInfo != null -> {
                NetworkInfoContent(
                    networkInfo = uiState.networkInfo!!,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun NetworkInfoContent(
    networkInfo: NetworkInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Connection Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (networkInfo.isConnected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Connection Status",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (networkInfo.isConnected) "Connected" else "Disconnected",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = networkInfo.networkType.name,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        InfoCard(
            title = "Network Type",
            value = networkInfo.networkType.name
        )

        networkInfo.ssid?.let { ssid ->
            InfoCard(
                title = "WiFi Network",
                value = ssid
            )
        }

        InfoCard(
            title = "Signal Strength",
            value = "${networkInfo.signalStrength}%"
        )

        networkInfo.ipv4Address?.let { ipv4 ->
            InfoCard(
                title = "IPv4 Address",
                value = ipv4
            )
        }

        networkInfo.ipv6Address?.let { ipv6 ->
            InfoCard(
                title = "IPv6 Address",
                value = ipv6
            )
        }

        InfoCard(
            title = "Internet Capable",
            value = if (networkInfo.hasInternet) "Yes" else "No"
        )

        InfoCard(
            title = "Metered Connection",
            value = if (networkInfo.isMetered) "Yes" else "No"
        )

        InfoCard(
            title = "VPN Connected",
            value = if (networkInfo.isVpnConnected) "Yes" else "No"
        )

        networkInfo.wifiFrequencyBand?.let { band ->
            InfoCard(
                title = "WiFi Frequency Band",
                value = when (band) {
                    WiFiFrequencyBand.BAND_2_4_GHZ -> "2.4 GHz"
                    WiFiFrequencyBand.BAND_5_GHZ -> "5 GHz"
                    WiFiFrequencyBand.BAND_6_GHZ -> "6 GHz"
                    WiFiFrequencyBand.UNKNOWN -> "Unknown"
                }
            )
        }

        networkInfo.cellularGeneration?.let { gen ->
            InfoCard(
                title = "Cellular Generation",
                value = when (gen) {
                    CellularGeneration.GENERATION_2G -> "2G"
                    CellularGeneration.GENERATION_3G -> "3G"
                    CellularGeneration.GENERATION_4G -> "4G (LTE)"
                    CellularGeneration.GENERATION_5G -> "5G"
                    CellularGeneration.UNKNOWN -> "Unknown"
                }
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NetworkScreenPreview() {
    PhonecheckerTheme {
        NetworkInfoContent(
            networkInfo = NetworkInfo(
                isConnected = true,
                networkType = NetworkType.WIFI,
                signalStrength = 95,
                isMetered = false,
                hasInternet = true,
                wifiFrequencyBand = WiFiFrequencyBand.BAND_5_GHZ,
                cellularGeneration = null,
                isVpnConnected = false,
                ssid = "MyHomeWiFi",
                ipv4Address = "192.168.1.100",
                ipv6Address = "fe80::1234:5678:9abc:def0"
            )
        )
    }
}
