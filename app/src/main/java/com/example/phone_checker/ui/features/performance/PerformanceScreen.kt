package com.example.phone_checker.ui.features.performance

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.phone_checker.data.repository.PerformanceInfo
import com.example.phone_checker.data.repository.PerformanceStatus
import com.example.phone_checker.ui.components.InfoRowCard
import com.example.phone_checker.ui.theme.PhonecheckerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: PerformanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance") },
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
            uiState.performanceInfo != null -> {
                PerformanceInfoContent(
                    performanceInfo = uiState.performanceInfo!!,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun PerformanceInfoContent(
    performanceInfo: PerformanceInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PerformanceStatusCard(
            status = performanceInfo.status,
            cpuUsagePercent = performanceInfo.cpuUsagePercent,
            ramUsagePercent = performanceInfo.ramUsagePercent
        )

        SectionHeader(title = "CPU")

        InfoRowCard(
            title = "CPU Usage",
            value = "${performanceInfo.cpuUsagePercent.toInt()}%"
        )

        InfoRowCard(
            title = "CPU Cores",
            value = "${performanceInfo.cpuCores}"
        )

        performanceInfo.cpuMaxFrequencyMhz?.let { maxFreq ->
            InfoRowCard(
                title = "CPU Max Frequency",
                value = "${maxFreq} MHz"
            )
        }

        performanceInfo.cpuCurrentFrequencyMhz?.let { currentFreq ->
            InfoRowCard(
                title = "CPU Current Frequency",
                value = "${currentFreq} MHz"
            )
        }

        SectionHeader(title = "Memory")

        InfoRowCard(
            title = "RAM Usage",
            value = "${performanceInfo.ramUsagePercent}%"
        )

        InfoRowCard(
            title = "Total RAM",
            value = "${performanceInfo.totalRamMb} MB"
        )

        InfoRowCard(
            title = "Available RAM",
            value = "${performanceInfo.availableRamMb} MB"
        )

        InfoRowCard(
            title = "Used RAM",
            value = "${performanceInfo.usedRamMb} MB"
        )

        InfoRowCard(
            title = "App Memory Usage",
            value = "${performanceInfo.appMemoryUsageMb.toInt()} MB"
        )

        InfoRowCard(
            title = "Native Heap",
            value = "${performanceInfo.nativeHeapMb.toInt()} MB"
        )

        InfoRowCard(
            title = "Dalvik Heap",
            value = "${performanceInfo.dalvikHeapMb.toInt()} MB"
        )

        SectionHeader(title = "Processes")

        InfoRowCard(
            title = "Active Threads",
            value = "${performanceInfo.threadCount}"
        )

        InfoRowCard(
            title = "Running Processes",
            value = "${performanceInfo.appProcesses}"
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun PerformanceStatusCard(
    status: PerformanceStatus,
    cpuUsagePercent: Float,
    ramUsagePercent: Int,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (status) {
        PerformanceStatus.EXCELLENT ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        PerformanceStatus.GOOD ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        PerformanceStatus.MODERATE ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        PerformanceStatus.POOR ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Performance Status",
                style = MaterialTheme.typography.titleMedium,
                color = contentColor
            )
            Text(
                text = status.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            MetricRow(
                label = "CPU Usage",
                value = "${cpuUsagePercent.toInt()}%",
                progress = (cpuUsagePercent / 100f).coerceIn(0f, 1f),
                color = contentColor
            )
            Spacer(modifier = Modifier.height(10.dp))
            MetricRow(
                label = "RAM Usage",
                value = "${ramUsagePercent}%",
                progress = (ramUsagePercent / 100f).coerceIn(0f, 1f),
                color = contentColor
            )
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    progress: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PerformanceScreenPreview() {
    PhonecheckerTheme {
        PerformanceInfoContent(
            performanceInfo = PerformanceInfo(
                cpuUsagePercent = 45.5f,
                cpuCores = 8,
                cpuMaxFrequencyMhz = 2840,
                cpuCurrentFrequencyMhz = 1800,
                totalRamMb = 8192,
                usedRamMb = 4096,
                availableRamMb = 4096,
                ramUsagePercent = 50,
                appMemoryUsageMb = 256.5f,
                nativeHeapMb = 128.3f,
                dalvikHeapMb = 128.2f,
                threadCount = 42,
                appProcesses = 25,
                status = PerformanceStatus.GOOD
            )
        )
    }
}
