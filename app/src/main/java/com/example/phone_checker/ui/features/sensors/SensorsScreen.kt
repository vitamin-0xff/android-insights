package com.example.phone_checker.ui.features.sensors

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.phone_checker.data.repository.SensorInfo
import com.example.phone_checker.data.repository.SensorStatus
import com.example.phone_checker.data.repository.SensorsHealthInfo
import com.example.phone_checker.data.repository.SensorsHealthStatus
import com.example.phone_checker.ui.theme.PhonecheckerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SensorsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensors") },
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
            uiState.sensorsInfo != null -> {
                SensorsInfoContent(
                    sensorsInfo = uiState.sensorsInfo!!,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun SensorsInfoContent(
    sensorsInfo: SensorsHealthInfo,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (sensorsInfo.status) {
                        SensorsHealthStatus.EXCELLENT -> MaterialTheme.colorScheme.primaryContainer
                        SensorsHealthStatus.GOOD -> MaterialTheme.colorScheme.secondaryContainer
                        SensorsHealthStatus.POOR -> MaterialTheme.colorScheme.errorContainer
                        SensorsHealthStatus.UNAVAILABLE -> MaterialTheme.colorScheme.surfaceVariant
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
                        text = "Sensors Health",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = sensorsInfo.status.name,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${sensorsInfo.activeSensors} of ${sensorsInfo.totalSensors} sensors available",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            // Recommendation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Recommendation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = sensorsInfo.recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        item {
            Text(
                text = "Key Sensors",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        sensorsInfo.accelerometer?.let { item { SensorCard(sensor = it) } }
        sensorsInfo.gyroscope?.let { item { SensorCard(sensor = it) } }
        sensorsInfo.magnetometer?.let { item { SensorCard(sensor = it) } }
        sensorsInfo.proximity?.let { item { SensorCard(sensor = it) } }
        sensorsInfo.ambientLight?.let { item { SensorCard(sensor = it) } }

        item {
            Text(
                text = "All Sensors (${sensorsInfo.allSensors.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(sensorsInfo.allSensors) { sensor ->
            SensorCard(sensor = sensor)
        }
    }
}

@Composable
private fun SensorCard(sensor: SensorInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (sensor.status) {
                SensorStatus.AVAILABLE -> MaterialTheme.colorScheme.primaryContainer
                SensorStatus.CALIBRATION_NEEDED -> MaterialTheme.colorScheme.tertiaryContainer
                SensorStatus.UNAVAILABLE -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sensor.type,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.5f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = sensor.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sensor.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.5f)
            )
            Text(
                text = "Vendor: ${sensor.vendor}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Power: ${sensor.power} mA",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Max Range: ${sensor.maxRange}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SensorsScreenPreview() {
    PhonecheckerTheme {
        SensorsInfoContent(
            sensorsInfo = SensorsHealthInfo(
                totalSensors = 15,
                activeSensors = 14,
                accelerometer = SensorInfo(
                    name = "BMI160 Accelerometer",
                    type = "Accelerometer",
                    vendor = "Bosch",
                    power = 0.18f,
                    maxRange = 78.4f,
                    resolution = 0.0024f,
                    isAvailable = true,
                    status = SensorStatus.AVAILABLE
                ),
                gyroscope = null,
                magnetometer = null,
                proximity = null,
                ambientLight = null,
                pressure = null,
                humidity = null,
                temperature = null,
                allSensors = emptyList(),
                status = SensorsHealthStatus.EXCELLENT,
                recommendation = "All critical sensors are functioning properly."
            )
        )
    }
}
