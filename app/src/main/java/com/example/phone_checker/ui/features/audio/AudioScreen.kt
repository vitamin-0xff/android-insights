package com.example.phone_checker.ui.features.audio

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.phone_checker.data.repository.AudioDeviceHealth
import com.example.phone_checker.data.repository.AudioHealthInfo
import com.example.phone_checker.data.repository.AudioHealthStatus
import com.example.phone_checker.data.repository.MicrophoneStatus
import com.example.phone_checker.ui.theme.PhonecheckerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioScreen(
    onNavigateBack: () -> Unit,
    viewModel: AudioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Health") },
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
            uiState.audioInfo != null -> {
                AudioInfoContent(
                    audioInfo = uiState.audioInfo!!,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun AudioInfoContent(
    audioInfo: AudioHealthInfo,
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
                    containerColor = when (audioInfo.status) {
                        AudioHealthStatus.HEALTHY -> MaterialTheme.colorScheme.primaryContainer
                        AudioHealthStatus.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                        AudioHealthStatus.CRITICAL -> MaterialTheme.colorScheme.errorContainer
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
                        text = "Audio System Health",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = audioInfo.status.name,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Volume: ${(audioInfo.speakerVolume * 100 / audioInfo.speakerMaxVolume)}%",
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
                        text = audioInfo.recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        item {
            Text(
                text = "Device Status",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            HealthCard(
                title = "Speaker Health",
                status = audioInfo.speakerHealth
            )
        }

        item {
            HealthCard(
                title = "Microphone Health",
                status = audioInfo.microphoneHealth
            )
        }

        item {
            HealthCard(
                title = "Headphone Health",
                status = audioInfo.headphoneHealth
            )
        }

        item {
            Text(
                text = "Audio Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            InfoCard(
                title = "Microphone Status",
                value = audioInfo.microphoneStatus.name
            )
        }

        item {
            InfoCard(
                title = "Headphone Connected",
                value = if (audioInfo.headphoneConnected) "Yes" else "No"
            )
        }

        item {
            InfoCard(
                title = "Bluetooth Audio",
                value = if (audioInfo.bluetoothAudioConnected) "Connected" else "Disconnected"
            )
        }

        item {
            InfoCard(
                title = "Music Active",
                value = if (audioInfo.musicActive) "Yes" else "No"
            )
        }

        item {
            InfoCard(
                title = "Call Active",
                value = if (audioInfo.callActive) "Yes" else "No"
            )
        }

        if (audioInfo.volumeWarnings.isNotEmpty()) {
            item {
                Text(
                    text = "Warnings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(audioInfo.volumeWarnings) { warning ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = warning,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthCard(
    title: String,
    status: AudioDeviceHealth
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                AudioDeviceHealth.EXCELLENT -> MaterialTheme.colorScheme.primaryContainer
                AudioDeviceHealth.GOOD -> MaterialTheme.colorScheme.secondaryContainer
                AudioDeviceHealth.FAIR -> MaterialTheme.colorScheme.tertiaryContainer
                AudioDeviceHealth.POOR -> MaterialTheme.colorScheme.errorContainer
            }
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
                fontWeight = FontWeight.Bold
            )
            Text(
                text = status.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
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
fun AudioScreenPreview() {
    PhonecheckerTheme {
        AudioInfoContent(
            audioInfo = AudioHealthInfo(
                speakerVolume = 7,
                speakerMaxVolume = 15,
                microphoneStatus = MicrophoneStatus.AVAILABLE,
                headphoneConnected = false,
                bluetoothAudioConnected = false,
                musicActive = false,
                callActive = false,
                recordingActive = false,
                speakerHealth = AudioDeviceHealth.GOOD,
                microphoneHealth = AudioDeviceHealth.EXCELLENT,
                headphoneHealth = AudioDeviceHealth.EXCELLENT,
                volumeWarnings = emptyList(),
                status = AudioHealthStatus.HEALTHY,
                recommendation = "Audio system is healthy. Using speaker output."
            )
        )
    }
}
