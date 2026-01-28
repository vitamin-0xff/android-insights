package com.example.phone_checker.ui.features.thermal

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.phone_checker.data.repository.ThermalInfo
import com.example.phone_checker.data.repository.ThermalStatus
import com.example.phone_checker.ui.theme.PhonecheckerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalScreen(
    onNavigateBack: () -> Unit,
    viewModel: ThermalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thermal Status") },
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
            uiState.thermalInfo != null -> {
                ThermalInfoContent(
                    thermalInfo = uiState.thermalInfo!!,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun ThermalInfoContent(
    thermalInfo: ThermalInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Temperature Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (thermalInfo.status) {
                    ThermalStatus.NORMAL -> MaterialTheme.colorScheme.primaryContainer
                    ThermalStatus.WARM -> MaterialTheme.colorScheme.tertiaryContainer
                    ThermalStatus.HOT -> MaterialTheme.colorScheme.errorContainer
                    ThermalStatus.CRITICAL -> MaterialTheme.colorScheme.error
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
                    text = "Device Temperature",
                    style = MaterialTheme.typography.titleMedium,
                    color = when (thermalInfo.status) {
                        ThermalStatus.CRITICAL -> MaterialTheme.colorScheme.onError
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${thermalInfo.batteryTemperature}°C",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (thermalInfo.status) {
                        ThermalStatus.CRITICAL -> MaterialTheme.colorScheme.onError
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = thermalInfo.status.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = when (thermalInfo.status) {
                        ThermalStatus.CRITICAL -> MaterialTheme.colorScheme.onError
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }

        // Recommendation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = thermalInfo.recommendation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Temperature Guide
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Temperature Guide",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                TempGuideItem("Normal", "< 35°C", "Optimal operating temperature")
                Spacer(modifier = Modifier.height(8.dp))
                TempGuideItem("Warm", "35-40°C", "Slightly elevated, monitor usage")
                Spacer(modifier = Modifier.height(8.dp))
                TempGuideItem("Hot", "40-45°C", "High temperature, reduce load")
                Spacer(modifier = Modifier.height(8.dp))
                TempGuideItem("Critical", "> 45°C", "Dangerous level, stop usage")
            }
        }
    }
}

@Composable
fun TempGuideItem(label: String, range: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        Text(
            text = range,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ThermalScreenPreview() {
    PhonecheckerTheme {
        ThermalInfoContent(
            thermalInfo = ThermalInfo(
                batteryTemperature = 36.5f,
                status = ThermalStatus.WARM,
                recommendation = "Device is getting warm. Consider reducing usage."
            )
        )
    }
}
