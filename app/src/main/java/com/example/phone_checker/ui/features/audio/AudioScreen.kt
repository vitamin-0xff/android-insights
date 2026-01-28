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
import com.example.phone_checker.data.monitors.AudioDevice
import com.example.phone_checker.data.monitors.AudioDeviceCategory
import com.example.phone_checker.data.monitors.AudioDeviceRole
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
                    inputDevices = uiState.inputDevices,
                    outputDevices = uiState.outputDevices,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun AudioInfoContent(
    audioInfo: AudioHealthInfo,
    inputDevices: List<AudioDevice> = emptyList(),
    outputDevices: List<AudioDevice> = emptyList(),
    modifier: Modifier = Modifier
) {
    // Filter out built-in devices to show only external devices
    val externalInputDevices = inputDevices.filter { it.category != AudioDeviceCategory.BUILT_IN }
    val externalOutputDevices = outputDevices.filter { it.category != AudioDeviceCategory.BUILT_IN }
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

        item {
            Text(
                text = "Connected Devices",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Device Statistics Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${externalOutputDevices.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Output",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${externalInputDevices.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Input",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        if (externalOutputDevices.isNotEmpty()) {
            item {
                Text(
                    text = "Output Devices",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(externalOutputDevices) { device ->
                DeviceCard(device = device)
            }
        } else {
            item {
                Text(
                    text = "No external output devices connected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (externalInputDevices.isNotEmpty()) {
            item {
                Text(
                    text = "Input Devices",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(externalInputDevices) { device ->
                DeviceCard(device = device)
            }
        } else {
            item {
                Text(
                    text = "No external input devices connected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(16.dp)
                )
            }
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

@Composable
private fun DeviceCard(device: AudioDevice) {
    val categoryColor = when (device.category) {
        AudioDeviceCategory.BLUETOOTH -> MaterialTheme.colorScheme.primaryContainer
        AudioDeviceCategory.WIRED -> MaterialTheme.colorScheme.secondaryContainer
        AudioDeviceCategory.USB -> MaterialTheme.colorScheme.tertiaryContainer
        AudioDeviceCategory.HDMI -> MaterialTheme.colorScheme.errorContainer
        AudioDeviceCategory.BUILT_IN -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val categoryTextColor = when (device.category) {
        AudioDeviceCategory.BLUETOOTH -> MaterialTheme.colorScheme.onPrimaryContainer
        AudioDeviceCategory.WIRED -> MaterialTheme.colorScheme.onSecondaryContainer
        AudioDeviceCategory.USB -> MaterialTheme.colorScheme.onTertiaryContainer
        AudioDeviceCategory.HDMI -> MaterialTheme.colorScheme.onErrorContainer
        AudioDeviceCategory.BUILT_IN -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = categoryColor)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryTextColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${device.category.name} • ${device.role.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = categoryTextColor
                    )
                }
            }

            if (device.sampleRates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sample Rates: ${device.sampleRates.joinToString(", ") { "$it Hz" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryTextColor
                )
            }

            if (device.channelCounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Channels: ${device.channelCounts.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryTextColor
                )
            }

            if (device.address.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Address: ${device.address}",
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryTextColor
                )
            }
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
                bluetoothAudioConnected = true,
                musicActive = false,
                callActive = false,
                recordingActive = false,
                speakerHealth = AudioDeviceHealth.GOOD,
                microphoneHealth = AudioDeviceHealth.EXCELLENT,
                headphoneHealth = AudioDeviceHealth.EXCELLENT,
                volumeWarnings = emptyList(),
                status = AudioHealthStatus.HEALTHY,
                recommendation = "Audio system is healthy. Using speaker output.",
                inputDevices = listOf(
                    AudioDevice(
                        id = 1,
                        name = "Built-in Microphone",
                        type = 15,
                        category = AudioDeviceCategory.BUILT_IN,
                        role = AudioDeviceRole.INPUT,
                        address = "",
                        isWireless = false,
                        sampleRates = intArrayOf(16000, 44100, 48000),
                        channelCounts = intArrayOf(1, 2),
                        connectionTime = 0L
                    )
                ),
                outputDevices = listOf(
                    AudioDevice(
                        id = 2,
                        name = "Built-in Speaker",
                        type = 1,
                        category = AudioDeviceCategory.BUILT_IN,
                        role = AudioDeviceRole.OUTPUT,
                        address = "",
                        isWireless = false,
                        sampleRates = intArrayOf(44100, 48000),
                        channelCounts = intArrayOf(2),
                        connectionTime = 0L
                    ),
                    AudioDevice(
                        id = 3,
                        name = "Bluetooth Headset",
                        type = 8,
                        category = AudioDeviceCategory.BLUETOOTH,
                        role = AudioDeviceRole.BIDIRECTIONAL,
                        address = "AA:BB:CC:DD:EE:FF",
                        isWireless = true,
                        sampleRates = intArrayOf(16000, 44100),
                        channelCounts = intArrayOf(1, 2),
                        connectionTime = System.currentTimeMillis()
                    )
                )
            )
        )
    }
}
