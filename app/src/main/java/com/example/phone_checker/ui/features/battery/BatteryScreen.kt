package com.example.phone_checker.ui.features.battery

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
import com.example.phone_checker.data.repository.BatteryHealth
import com.example.phone_checker.data.repository.BatteryInfo
import com.example.phone_checker.data.repository.BatteryStatus
import com.example.phone_checker.ui.theme.PhonecheckerTheme
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryScreen(
    onNavigateBack: () -> Unit,
    viewModel: BatteryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battery Health") },
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
            uiState.batteryInfo != null -> {
                BatteryInfoContent(
                    batteryInfo = uiState.batteryInfo!!,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun BatteryInfoContent(
    batteryInfo: BatteryInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Battery Level Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    batteryInfo.level >= 70 -> MaterialTheme.colorScheme.primaryContainer
                    batteryInfo.level >= 30 -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
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
                    text = "Battery Level",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${batteryInfo.level}%",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (batteryInfo.isCharging) "Charging" else "On Battery",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Details Cards
        InfoCard(
            title = "Health",
            value = batteryInfo.health.name.replace("_", " ")
        )
        
        InfoCard(
            title = "Status",
            value = batteryInfo.status.name.replace("_", " ")
        )

        InfoCard(
            title = "Temperature",
            value = "${batteryInfo.temperature}°C"
        )

        InfoCard(
            title = "Voltage",
            value = "${batteryInfo.voltage / 1000f}V"
        )

        InfoCard(
            title = "Technology",
            value = batteryInfo.technology
        )

        // Battery Health Section
        Text(
            text = "Battery Health",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Capacity Health Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    batteryInfo.capacityPercent >= 80 -> MaterialTheme.colorScheme.primaryContainer
                    batteryInfo.capacityPercent >= 60 -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
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
                    text = "Battery Capacity",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${batteryInfo.capacityPercent}%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        batteryInfo.capacityPercent >= 80 -> "Excellent Health"
                        batteryInfo.capacityPercent >= 60 -> "Good Health"
                        else -> "Consider Replacement"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        batteryInfo.chargeCounterMah?.let {
            InfoCard(
                title = "Current Charge",
                value = "$it mAh"
            )
        }

        batteryInfo.currentNowMa?.let { current ->
            val currentAbs = abs(current)
            InfoCard(
                title = if (current > 0) "Charging Current" else "Discharge Current",
                value = "$currentAbs mA"
            )
        }

        batteryInfo.currentAverageMa?.let { current ->
            val currentAbs = abs(current)
            InfoCard(
                title = "Average Current",
                value = "$currentAbs mA"
            )
        }

        batteryInfo.energyCounterNwh?.let {
            val energyMwh = it / 1000000 // Convert nWh to mWh
            InfoCard(
                title = "Remaining Energy",
                value = "$energyMwh mWh"
            )
        }

        batteryInfo.cycleCount?.let {
            InfoCard(
                title = "Charge Cycles",
                value = "$it cycles"
            )
        }
    }
}

@Composable
fun InfoCard(
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
fun BatteryScreenPreview() {
    PhonecheckerTheme {
        BatteryInfoContent(
            batteryInfo = BatteryInfo(
                level = 85,
                temperature = 32.5f,
                voltage = 4200,
                health = BatteryHealth.GOOD,
                status = BatteryStatus.CHARGING,
                isCharging = true,
                technology = "Li-ion",
                capacityPercent = 92,
                chargeCounterMah = 3450,
                currentNowMa = 1200,
                currentAverageMa = 980,
                energyCounterNwh = 15000000000,
                cycleCount = 125
            )
        )
    }
}
