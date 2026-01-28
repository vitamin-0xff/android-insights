package com.example.phone_checker.ui.features.screen

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
import android.view.Display
import android.view.Surface
import com.example.phone_checker.data.repository.ScreenHealthInfo
import com.example.phone_checker.ui.theme.PhonecheckerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenHealthScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScreenHealthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Screen") },
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
            uiState.screenHealthInfo != null -> {
                ScreenHealthInfoContent(
                    screenHealthInfo = uiState.screenHealthInfo!!,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun ScreenHealthInfoContent(
    screenHealthInfo: ScreenHealthInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Display Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = screenHealthInfo.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${screenHealthInfo.widthPixels} x ${screenHealthInfo.heightPixels}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${formatRefreshRate(screenHealthInfo.refreshRate)} Hz",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Display Capabilities Card
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
                    text = "Display Capabilities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildString {
                        append("HDR: ")
                        append(if (screenHealthInfo.isHdr) "Supported" else "Not supported")
                        append(" • Wide Color Gamut: ")
                        append(if (screenHealthInfo.isWideColorGamut) "Supported" else "Not supported")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        InfoCard(
            title = "Suggested Frame Rate",
            value = formatSuggestedFrameRate(screenHealthInfo.suggestedFrameRate)
        )

        InfoCard(
            title = "Supported Refresh Rates",
            value = formatSupportedRates(screenHealthInfo.supportedRefreshRates)
        )

        InfoCard(
            title = "Density",
            value = "${screenHealthInfo.densityDpi} dpi"
        )

        InfoCard(
            title = "Rotation",
            value = formatRotation(screenHealthInfo.rotation)
        )

        InfoCard(
            title = "Display State",
            value = formatDisplayState(screenHealthInfo.displayState)
        )

        InfoCard(
            title = "Display Count",
            value = screenHealthInfo.displayCount.toString()
        )

        // DPI and Pixel Density Section
        Text(
            text = "DPI & Pixel Density",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        InfoCard(
            title = "Horizontal DPI (X)",
            value = String.format("%.2f", screenHealthInfo.xDpi)
        )

        InfoCard(
            title = "Vertical DPI (Y)",
            value = String.format("%.2f", screenHealthInfo.yDpi)
        )

        InfoCard(
            title = "Density",
            value = String.format("%.2f", screenHealthInfo.density)
        )

        InfoCard(
            title = "Scaled Density",
            value = String.format("%.2f", screenHealthInfo.scaledDensity)
        )

        // Physical Dimensions Section
        Text(
            text = "Physical Dimensions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        InfoCard(
            title = "Aspect Ratio",
            value = screenHealthInfo.aspectRatio
        )

        InfoCard(
            title = "Width (Physical)",
            value = String.format("%.2f inches", screenHealthInfo.physicalWidth)
        )

        InfoCard(
            title = "Height (Physical)",
            value = String.format("%.2f inches", screenHealthInfo.physicalHeight)
        )

        InfoCard(
            title = "Diagonal Size",
            value = String.format("%.2f inches", screenHealthInfo.diagonalInches)
        )
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
fun ScreenHealthScreenPreview() {
    PhonecheckerTheme {
        ScreenHealthInfoContent(
            screenHealthInfo = ScreenHealthInfo(
                displayName = "Built-in Display",
                widthPixels = 1080,
                heightPixels = 2400,
                densityDpi = 420,
                refreshRate = 120f,
                suggestedFrameRate = 90f,
                supportedRefreshRates = listOf(60f, 90f, 120f),
                rotation = Surface.ROTATION_0,
                displayState = Display.STATE_ON,
                isHdr = true,
                isWideColorGamut = true,
                displayCount = 1,
                xDpi = 420f,
                yDpi = 420f,
                scaledDensity = 2.625f,
                density = 2.625f,
                physicalWidth = 2.57f,
                physicalHeight = 5.71f,
                diagonalInches = 6.3f,
                aspectRatio = "9:20"
            )
        )
    }
}

private fun formatRotation(rotation: Int): String = when (rotation) {
    Surface.ROTATION_0 -> "0°"
    Surface.ROTATION_90 -> "90°"
    Surface.ROTATION_180 -> "180°"
    Surface.ROTATION_270 -> "270°"
    else -> "Unknown"
}

private fun formatDisplayState(state: Int): String = when (state) {
    Display.STATE_ON -> "On"
    Display.STATE_OFF -> "Off"
    Display.STATE_DOZE -> "Doze"
    Display.STATE_DOZE_SUSPEND -> "Doze Suspend"
    Display.STATE_UNKNOWN -> "Unknown"
    else -> "Unknown"
}

private fun formatRefreshRate(rate: Float): String = if (rate > 0f) {
    rate.toInt().toString()
} else {
    "N/A"
}

private fun formatSuggestedFrameRate(rate: Float): String = if (rate > 0f) {
    "${rate.toInt()} Hz"
} else {
    "Not available"
}

private fun formatSupportedRates(rates: List<Float>): String = if (rates.isNotEmpty()) {
    rates.joinToString(", ") { "${it.toInt()} Hz" }
} else {
    "Not available"
}
