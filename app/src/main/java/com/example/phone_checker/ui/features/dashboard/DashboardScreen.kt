package com.example.phone_checker.ui.features.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.phone_checker.ui.theme.PhonecheckerTheme

data class HealthMetric(
    val title: String,
    val value: String,
    val status: HealthStatus,
    val icon: ImageVector,
    val route: String? = null
)

enum class HealthStatus {
    GOOD, WARNING, CRITICAL, UNKNOWN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onMetricClick: (String) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val batteryStatus = when {
        uiState.batteryLevel >= 70 -> HealthStatus.GOOD
        uiState.batteryLevel >= 30 -> HealthStatus.WARNING
        else -> HealthStatus.CRITICAL
    }
    
    val tempStatus = when {
        uiState.temperature < 35f -> HealthStatus.GOOD
        uiState.temperature < 40f -> HealthStatus.WARNING
        else -> HealthStatus.CRITICAL
    }
    
    val storageStatus = when {
        uiState.storageUsage < 70 -> HealthStatus.GOOD
        uiState.storageUsage < 90 -> HealthStatus.WARNING
        else -> HealthStatus.CRITICAL
    }
    
    val ramStatus = when {
        uiState.ramUsage < 70 -> HealthStatus.GOOD
        uiState.ramUsage < 85 -> HealthStatus.WARNING
        else -> HealthStatus.CRITICAL
    }
    
    val networkStatus = when (uiState.networkQuality) {
        "EXCELLENT", "GOOD" -> HealthStatus.GOOD
        "FAIR" -> HealthStatus.WARNING
        else -> HealthStatus.CRITICAL
    }
    
    val screenStatus = when {
        uiState.screenOnTimeMinutes < 180 -> HealthStatus.GOOD
        uiState.screenOnTimeMinutes < 360 -> HealthStatus.WARNING
        else -> HealthStatus.CRITICAL
    }
    
    val appsStatus = when {
        uiState.appsRunning < 30 -> HealthStatus.GOOD
        uiState.appsRunning < 50 -> HealthStatus.WARNING
        else -> HealthStatus.CRITICAL
    }
    
    val sensorsStatus = when (uiState.sensorsHealth) {
        "EXCELLENT" -> HealthStatus.GOOD
        "GOOD" -> HealthStatus.GOOD
        "POOR" -> HealthStatus.WARNING
        else -> HealthStatus.UNKNOWN
    }
    
    val audioStatus = when (uiState.audioHealth) {
        "HEALTHY" -> HealthStatus.GOOD
        "WARNING" -> HealthStatus.WARNING
        "CRITICAL" -> HealthStatus.CRITICAL
        else -> HealthStatus.UNKNOWN
    }
    
    val screenTimeHours = uiState.screenOnTimeMinutes / 60
    val screenTimeMins = uiState.screenOnTimeMinutes % 60
    
    val metrics = listOf(
        HealthMetric("Battery", "${uiState.batteryLevel}%", batteryStatus, Icons.Default.Build, "battery"),
        HealthMetric("Temperature", "${uiState.temperature.toInt()}°C", tempStatus, Icons.Default.Info, "thermal"),
        HealthMetric("RAM", "${uiState.ramUsage}%", ramStatus, Icons.Default.Star, "performance"),
        HealthMetric("Storage", "${uiState.storageUsage}%", storageStatus, Icons.Default.Home, "storage"),
        HealthMetric("Network", uiState.networkType, networkStatus, Icons.Default.Settings, "network"),
        HealthMetric("Screen Time", "${screenTimeHours}h ${screenTimeMins}m", screenStatus, Icons.Default.Phone, "screen_health"),
        HealthMetric("Running Apps", "${uiState.appsRunning}", appsStatus, Icons.Default.List, "app_behavior"),
        HealthMetric("Sensors", uiState.sensorsHealth, sensorsStatus, Icons.Default.Settings, "sensors"),
        HealthMetric("I/O", "${uiState.audioInputDevices}in/${uiState.audioOutputDevices}out", audioStatus, Icons.Default.Settings, "devices"),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Device Health",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Overall: ${uiState.overallHealth}",
                                style = MaterialTheme.typography.bodySmall,
                                color = when (uiState.overallHealth) {
                                    "Critical" -> MaterialTheme.colorScheme.error
                                    "Needs Attention" -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                            if (uiState.criticalIssues > 0 || uiState.warnings > 0) {
                                Text(
                                    text = "• ${uiState.criticalIssues} critical, ${uiState.warnings} warnings",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onMetricClick("insights") }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "View Insights",
                            tint = if (uiState.criticalIssues > 0) {
                                MaterialTheme.colorScheme.error
                            } else if (uiState.warnings > 0) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Error loading data",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(metrics) { metric ->
                        HealthMetricCard(
                            metric = metric,
                            onClick = {
                                metric.route?.let { onMetricClick(it) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HealthMetricCard(
    metric: HealthMetric,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = metric.route != null,
        colors = CardDefaults.cardColors(
            containerColor = when (metric.status) {
                HealthStatus.GOOD -> MaterialTheme.colorScheme.primaryContainer
                HealthStatus.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                HealthStatus.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                HealthStatus.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = metric.icon,
                contentDescription = metric.title,
                tint = when (metric.status) {
                    HealthStatus.GOOD -> MaterialTheme.colorScheme.onPrimaryContainer
                    HealthStatus.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
                    HealthStatus.CRITICAL -> MaterialTheme.colorScheme.onErrorContainer
                    HealthStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = metric.title,
                style = MaterialTheme.typography.labelMedium,
                color = when (metric.status) {
                    HealthStatus.GOOD -> MaterialTheme.colorScheme.onPrimaryContainer
                    HealthStatus.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
                    HealthStatus.CRITICAL -> MaterialTheme.colorScheme.onErrorContainer
                    HealthStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = metric.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = when (metric.status) {
                    HealthStatus.GOOD -> MaterialTheme.colorScheme.onPrimaryContainer
                    HealthStatus.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
                    HealthStatus.CRITICAL -> MaterialTheme.colorScheme.onErrorContainer
                    HealthStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    PhonecheckerTheme {
        DashboardScreen()
    }
}