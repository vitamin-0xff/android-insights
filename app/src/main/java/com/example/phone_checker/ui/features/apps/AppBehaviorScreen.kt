package com.example.phone_checker.ui.features.apps

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
import com.example.phone_checker.data.repository.AppBehaviorInfo
import com.example.phone_checker.data.repository.AppBehaviorStatus
import com.example.phone_checker.data.repository.AppDrain
import com.example.phone_checker.data.repository.AppMemoryInfo
import com.example.phone_checker.data.repository.AppUpdateInfo
import com.example.phone_checker.data.repository.UsageInterval
import com.example.phone_checker.ui.theme.PhonecheckerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBehaviorScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppBehaviorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Behavior") },
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
            uiState.appBehaviorInfo != null -> {
                AppBehaviorInfoContent(
                    appBehaviorInfo = uiState.appBehaviorInfo!!,
                    selectedInterval = uiState.selectedInterval,
                    onIntervalChange = { viewModel.changeInterval(it) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun AppBehaviorInfoContent(
    appBehaviorInfo: AppBehaviorInfo,
    selectedInterval: UsageInterval,
    onIntervalChange: (UsageInterval) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Interval Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Usage Time Period",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UsageInterval.values().forEach { interval ->
                            FilterChip(
                                selected = selectedInterval == interval,
                                onClick = { onIntervalChange(interval) },
                                label = { 
                                    Text(
                                        interval.displayName,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (appBehaviorInfo.status) {
                        AppBehaviorStatus.GOOD -> MaterialTheme.colorScheme.primaryContainer
                        AppBehaviorStatus.MODERATE -> MaterialTheme.colorScheme.tertiaryContainer
                        AppBehaviorStatus.CONCERNING -> MaterialTheme.colorScheme.errorContainer
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
                        text = "App Performance",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = appBehaviorInfo.status.name,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${appBehaviorInfo.runningApps} apps running",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Text(
                text = "App Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            InfoCard(
                title = "Total Apps",
                value = "${appBehaviorInfo.totalAppsInstalled}"
            )
        }

        item {
            InfoCard(
                title = "User Apps",
                value = "${appBehaviorInfo.userApps}"
            )
        }

        item {
            InfoCard(
                title = "System Apps",
                value = "${appBehaviorInfo.systemApps}"
            )
        }

        item {
            InfoCard(
                title = "Running Apps",
                value = "${appBehaviorInfo.runningApps}"
            )
        }

        item {
            InfoCard(
                title = "Launchable Apps",
                value = "${appBehaviorInfo.launchableApps}"
            )
        }

        item {
            InfoCard(
                title = "Disabled Apps",
                value = "${appBehaviorInfo.disabledApps}"
            )
        }

        item {
            InfoCard(
                title = "Total Memory Usage",
                value = "${appBehaviorInfo.totalMemoryUsageMb} MB"
            )
        }

        item {
            InfoCard(
                title = "Recently Updated (7 days)",
                value = "${appBehaviorInfo.recentlyUpdatedApps}"
            )
        }

        if (appBehaviorInfo.topMemoryApps.isNotEmpty()) {
            item {
                Text(
                    text = "Top Memory Consuming Apps",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(appBehaviorInfo.topMemoryApps) { app ->
                AppMemoryCard(app = app)
            }
        }

        if (appBehaviorInfo.topDrainApps.isNotEmpty()) {
            item {
                Text(
                    text = "Top Battery Draining Apps",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(appBehaviorInfo.topDrainApps) { app ->
                AppDrainCard(app = app)
            }
        }

        if (appBehaviorInfo.recentUpdates.isNotEmpty()) {
            item {
                Text(
                    text = "Recently Updated Apps",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(appBehaviorInfo.recentUpdates) { app ->
                AppUpdateCard(app = app)
            }
        }
    }
}

@Composable
private fun AppMemoryCard(app: AppMemoryInfo) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${app.memoryUsageMb} MB",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Memory",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppUpdateCard(app: AppUpdateInfo) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = when (app.updateDaysAgo) {
                            0 -> "Today"
                            1 -> "Yesterday"
                            else -> "${app.updateDaysAgo} days ago"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Updated",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppDrainCard(app: AppDrain) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    val hours = app.usageTimeMinutes / 60
                    val minutes = app.usageTimeMinutes % 60
                    Text(
                        text = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
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
fun AppBehaviorScreenPreview() {
    PhonecheckerTheme {
        AppBehaviorInfoContent(
            appBehaviorInfo = AppBehaviorInfo(
                totalAppsInstalled = 150,
                systemApps = 80,
                userApps = 70,
                runningApps = 25,
                launchableApps = 45,
                disabledApps = 5,
                recentlyUpdatedApps = 8,
                totalMemoryUsageMb = 2048,
                topDrainApps = listOf(
                    AppDrain("Chrome", "com.android.chrome", 125),
                    AppDrain("YouTube", "com.google.android.youtube", 95),
                    AppDrain("Instagram", "com.instagram.android", 65)
                ),
                topMemoryApps = listOf(
                    AppMemoryInfo("Chrome", "com.android.chrome", 450),
                    AppMemoryInfo("Facebook", "com.facebook.katana", 320),
                    AppMemoryInfo("YouTube", "com.google.android.youtube", 280)
                ),
                recentUpdates = listOf(
                    AppUpdateInfo("WhatsApp", "com.whatsapp", 0),
                    AppUpdateInfo("Instagram", "com.instagram.android", 2),
                    AppUpdateInfo("Twitter", "com.twitter.android", 5)
                ),
                usageInterval = UsageInterval.TODAY,
                status = AppBehaviorStatus.MODERATE
            ),
            selectedInterval = UsageInterval.TODAY,
            onIntervalChange = {}
        )
    }
}
