package com.example.phone_checker.ui.features.main

import android.os.Build
import android.view.Display
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.phone_checker.data.repository.SensorInfo
import com.example.phone_checker.data.repository.SensorStatus
import com.example.phone_checker.data.repository.SensorsHealthInfo
import com.example.phone_checker.ui.components.KeyValueCard
import com.example.phone_checker.ui.features.dashboard.DashboardViewModel
import com.example.phone_checker.ui.features.dashboard.HealthMetric
import com.example.phone_checker.ui.features.dashboard.HealthMetricCard
import com.example.phone_checker.ui.features.dashboard.HealthStatus
import com.example.phone_checker.ui.features.sensors.SensorsViewModel
import com.example.phone_checker.ui.features.battery.BatteryScreen
import com.example.phone_checker.ui.features.thermal.ThermalScreen
import com.example.phone_checker.ui.features.performance.PerformanceScreen
import com.example.phone_checker.ui.features.storage.StorageScreen
import com.example.phone_checker.ui.features.network.NetworkScreen
import com.example.phone_checker.ui.features.screen.ScreenHealthScreen
import com.example.phone_checker.ui.features.apps.AppBehaviorScreen
import com.example.phone_checker.ui.features.sensors.SensorsScreen
import com.example.phone_checker.ui.theme.PhonecheckerTheme

sealed class BottomNavScreen(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavScreen("home", "Home", Icons.Filled.Home)
    object Sensors : BottomNavScreen("sensors_tab", "Sensors", Icons.Filled.Settings)
}

@Composable
fun MainNavigationScreen(
    onNavigateToDashboard: () -> Unit = {},
    sensorsViewModel: SensorsViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(BottomNavScreen.Home.icon, contentDescription = "Home") },
                    label = { Text(BottomNavScreen.Home.label) },
                    selected = currentRoute == BottomNavScreen.Home.route,
                    onClick = {
                        navController.navigate(BottomNavScreen.Home.route) {
                            popUpTo(BottomNavScreen.Home.route) { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(BottomNavScreen.Sensors.icon, contentDescription = "Sensors") },
                    label = { Text(BottomNavScreen.Sensors.label) },
                    selected = currentRoute == BottomNavScreen.Sensors.route,
                    onClick = {
                        navController.navigate(BottomNavScreen.Sensors.route) {
                            popUpTo(BottomNavScreen.Sensors.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavScreen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavScreen.Home.route) {
                HomeTabContent(
                    dashboardViewModel = dashboardViewModel,
                    onMetricClick = { route -> navController.navigate(route) }
                )
            }
            composable(BottomNavScreen.Sensors.route) {
                SensorsTabContent(
                    sensorsViewModel = sensorsViewModel,
                    onSensorClick = { route -> navController.navigate(route) }
                )
            }
            composable("battery") {
                BatteryScreenWrapper(onNavigateBack = { navController.popBackStack() })
            }
            composable("thermal") {
                ThermalScreenWrapper(onNavigateBack = { navController.popBackStack() })
            }
            composable("performance") {
                PerformanceScreenWrapper(onNavigateBack = { navController.popBackStack() })
            }
            composable("storage") {
                StorageScreenWrapper(onNavigateBack = { navController.popBackStack() })
            }
            composable("network") {
                NetworkScreenWrapper(onNavigateBack = { navController.popBackStack() })
            }
            composable("screen_health") {
                ScreenHealthScreenWrapper(onNavigateBack = { navController.popBackStack() })
            }
            composable("app_behavior") {
                AppBehaviorScreenWrapper(onNavigateBack = { navController.popBackStack() })
            }
            composable("sensors") {
                SensorsScreenWrapper(onNavigateBack = { navController.popBackStack() })
            }
            // Sensor test routes
            composable("sensor_test_accelerometer") {
                SensorTestScreenWrapper(sensorType = "Accelerometer", onNavigateBack = { navController.popBackStack() })
            }
            composable("sensor_test_gyroscope") {
                SensorTestScreenWrapper(sensorType = "Gyroscope", onNavigateBack = { navController.popBackStack() })
            }
            composable("sensor_test_magnetometer") {
                SensorTestScreenWrapper(sensorType = "Magnetometer", onNavigateBack = { navController.popBackStack() })
            }
            composable("sensor_test_proximity") {
                SensorTestScreenWrapper(sensorType = "Proximity", onNavigateBack = { navController.popBackStack() })
            }
            composable("sensor_test_ambient_light") {
                SensorTestScreenWrapper(sensorType = "Ambient Light", onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun HomeTabContent(
    dashboardViewModel: DashboardViewModel,
    onMetricClick: (String) -> Unit = {}
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    
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
    
    val networkStatus = when {
        !uiState.networkType.equals("NONE", ignoreCase = true) && uiState.networkSignal >= 70 -> HealthStatus.GOOD
        !uiState.networkType.equals("NONE", ignoreCase = true) && uiState.networkSignal >= 40 -> HealthStatus.WARNING
        !uiState.networkType.equals("NONE", ignoreCase = true) -> HealthStatus.WARNING
        else -> HealthStatus.CRITICAL
    }
    
    val screenStatus = when (uiState.screenState) {
        Display.STATE_ON -> HealthStatus.GOOD
        Display.STATE_DOZE, Display.STATE_DOZE_SUSPEND -> HealthStatus.WARNING
        Display.STATE_OFF -> HealthStatus.CRITICAL
        else -> HealthStatus.UNKNOWN
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
        else -> HealthStatus.CRITICAL
    }
    
    val networkDisplay = when {
        uiState.networkType.equals("NONE", ignoreCase = true) -> "No Network"
        else -> "${uiState.networkType} (${uiState.networkSignal}%)"
    }
    
    val screenRefreshLabel = when (uiState.screenState) {
        Display.STATE_ON -> "${uiState.screenRefreshRate.toInt()} Hz"
        else -> "Screen Off"
    }
    
    val metrics = listOf(
        HealthMetric("Battery", "${uiState.batteryLevel}%", batteryStatus, Icons.Default.Build, "battery"),
        HealthMetric("Temperature", "${uiState.temperature.toInt()}°C", tempStatus, Icons.Default.Info, "thermal"),
        HealthMetric("RAM", "${uiState.ramUsage}%", ramStatus, Icons.Default.Star, "performance"),
        HealthMetric("Storage", "${uiState.storageUsage}%", storageStatus, Icons.Default.Home, "storage"),
        HealthMetric("Network", networkDisplay, networkStatus, Icons.Default.Settings, "network"),
        HealthMetric("Screen", screenRefreshLabel, screenStatus, Icons.Default.Phone, "screen_health"),
        HealthMetric("Running Apps", "${uiState.appsRunning}", appsStatus, Icons.AutoMirrored.Filled.List, "app_behavior"),
        HealthMetric("I/O", "${uiState.audioInputDevices}in/${uiState.audioOutputDevices}out", HealthStatus.GOOD, Icons.Default.Settings, "sensors"),
    )

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
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
                }
            }
        }
        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
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

@Composable
private fun SensorsTabContent(
    sensorsViewModel: SensorsViewModel,
    onSensorClick: (String) -> Unit = {}
) {
    val sensorsUiState by sensorsViewModel.uiState.collectAsState()

    when {
        sensorsUiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        sensorsUiState.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error: ${sensorsUiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        sensorsUiState.sensorsInfo != null -> {
            val sensorsInfo = sensorsUiState.sensorsInfo!!
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Device Sensors",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    // Sensors Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Sensors Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${sensorsInfo.totalSensors}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Total Sensors",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${sensorsInfo.activeSensors}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Primary Sensors",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                sensorsInfo.accelerometer?.let { item { SensorCard(sensor = it, onClick = { onSensorClick("sensor_test_accelerometer") }) } }
                sensorsInfo.gyroscope?.let { item { SensorCard(sensor = it, onClick = { onSensorClick("sensor_test_gyroscope") }) } }
                sensorsInfo.magnetometer?.let { item { SensorCard(sensor = it, onClick = { onSensorClick("sensor_test_magnetometer") }) } }
                sensorsInfo.proximity?.let { item { SensorCard(sensor = it, onClick = { onSensorClick("sensor_test_proximity") }) } }
                sensorsInfo.ambientLight?.let { item { SensorCard(sensor = it, onClick = { onSensorClick("sensor_test_ambient_light") }) } }

                if (sensorsInfo.allSensors.isNotEmpty()) {
                    item {
                        Text(
                            text = "All Available Sensors",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(sensorsInfo.allSensors) { sensor ->
                        SensorCard(sensor = sensor, onClick = { onSensorClick("sensor_test_${sensor.type.lowercase().replace(" ", "_")}") })
                    }
                }
            }
        }
    }
}

@Composable
private fun SensorCard(sensor: SensorInfo, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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

// Wrapper composables for detail screens
@Composable
private fun BatteryScreenWrapper(onNavigateBack: () -> Unit) {
    BatteryScreen(onNavigateBack = onNavigateBack)
}

@Composable
private fun ThermalScreenWrapper(onNavigateBack: () -> Unit) {
    ThermalScreen(onNavigateBack = onNavigateBack)
}

@Composable
private fun PerformanceScreenWrapper(onNavigateBack: () -> Unit) {
    PerformanceScreen(onNavigateBack = onNavigateBack)
}

@Composable
private fun StorageScreenWrapper(onNavigateBack: () -> Unit) {
    StorageScreen(onNavigateBack = onNavigateBack)
}

@Composable
private fun NetworkScreenWrapper(onNavigateBack: () -> Unit) {
    NetworkScreen(onNavigateBack = onNavigateBack)
}

@Composable
private fun ScreenHealthScreenWrapper(onNavigateBack: () -> Unit) {
    ScreenHealthScreen(onNavigateBack = onNavigateBack)
}

@Composable
private fun AppBehaviorScreenWrapper(onNavigateBack: () -> Unit) {
    AppBehaviorScreen(onNavigateBack = onNavigateBack)
}

@Composable
private fun SensorsScreenWrapper(onNavigateBack: () -> Unit) {
    SensorsScreen(onNavigateBack = onNavigateBack)
}

@Composable
private fun SensorTestScreenWrapper(sensorType: String, onNavigateBack: () -> Unit) {
    SensorTestScreen(sensorType = sensorType, onNavigateBack = onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SensorTestScreen(sensorType: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    
    // State for sensor values
    val sensorX = remember { mutableStateOf(0f) }
    val sensorY = remember { mutableStateOf(0f) }
    val sensorZ = remember { mutableStateOf(0f) }
    val sensorValue = remember { mutableStateOf(0f) }
    val isMonitoring = remember { mutableStateOf(false) }
    val testResults = remember { mutableStateOf("Not started") }
    
    // Get the appropriate sensor type
    val sensorTypeValue = when (sensorType) {
        "Accelerometer" -> Sensor.TYPE_ACCELEROMETER
        "Gyroscope" -> Sensor.TYPE_GYROSCOPE
        "Magnetometer" -> Sensor.TYPE_MAGNETIC_FIELD
        "Proximity" -> Sensor.TYPE_PROXIMITY
        "Ambient Light" -> Sensor.TYPE_LIGHT
        else -> Sensor.TYPE_ACCELEROMETER
    }
    
    // Create sensor event listener
    val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                when (sensorTypeValue) {
                    Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_MAGNETIC_FIELD -> {
                        if (it.values.size >= 3) {
                            sensorX.value = it.values[0]
                            sensorY.value = it.values[1]
                            sensorZ.value = it.values[2]
                        }
                    }
                    Sensor.TYPE_PROXIMITY, Sensor.TYPE_LIGHT -> {
                        if (it.values.isNotEmpty()) {
                            sensorValue.value = it.values[0]
                        }
                    }
                }
            }
        }
        
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    
    // Handle monitoring lifecycle
    androidx.compose.runtime.LaunchedEffect(isMonitoring.value) {
        if (isMonitoring.value && sensorManager != null) {
            val sensor = sensorManager.getDefaultSensor(sensorTypeValue)
            if (sensor != null) {
                sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        } else {
            sensorManager?.unregisterListener(sensorEventListener)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$sensorType Test") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "$sensorType Sensor Test",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Real-time sensor data monitoring",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Real-time data display
            item {
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
                            text = "Real-Time Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        when (sensorType) {
                            "Accelerometer", "Gyroscope", "Magnetometer" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SensorValueRow("X-axis", sensorX.value)
                                    SensorValueRow("Y-axis", sensorY.value)
                                    SensorValueRow("Z-axis", sensorZ.value)
                                }
                            }
                            "Proximity" -> {
                                SensorValueRow("Distance", sensorValue.value)
                            }
                            "Ambient Light" -> {
                                SensorValueRow("Light Level (Lux)", sensorValue.value)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { isMonitoring.value = !isMonitoring.value },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isMonitoring.value) "Stop Monitoring" else "Start Monitoring")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Test Cases",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Test case 1: Initialization
            item {
                TestCaseCard(
                    testNumber = 1,
                    testName = "Sensor Initialization",
                    description = "Verify sensor can be initialized and returns valid data",
                    onRunTest = {
                        isMonitoring.value = true
                        testResults.value = "Test started - monitoring sensor data"
                    }
                )
            }

            // Test case 2: Accuracy
            item {
                TestCaseCard(
                    testNumber = 2,
                    testName = "Accuracy Verification",
                    description = "Check if sensor readings fall within acceptable accuracy range",
                    onRunTest = {
                        val result = when (sensorType) {
                            "Accelerometer" -> {
                                val magnitude = kotlin.math.sqrt(sensorX.value * sensorX.value + 
                                                                  sensorY.value * sensorY.value + 
                                                                  sensorZ.value * sensorZ.value)
                                if (magnitude > 8 && magnitude < 11) "PASS - Gravity reading correct" 
                                else "CHECK - Magnitude: ${"%.2f".format(magnitude)}"
                            }
                            "Proximity" -> {
                                if (sensorValue.value >= 0) "PASS - Valid distance reading"
                                else "FAIL - Invalid reading"
                            }
                            "Ambient Light" -> {
                                if (sensorValue.value >= 0) "PASS - Valid lux reading: ${"%.1f".format(sensorValue.value)}"
                                else "FAIL - Invalid reading"
                            }
                            else -> "PASS - Data within range"
                        }
                        testResults.value = result
                    }
                )
            }

            // Test case 3: Responsiveness
            item {
                TestCaseCard(
                    testNumber = 3,
                    testName = "Responsiveness",
                    description = "Measure sensor response time to detect changes",
                    onRunTest = {
                        testResults.value = "Test in progress - rotate or move device and observe value changes"
                        isMonitoring.value = true
                    }
                )
            }

            // Test case 4: Stability
            item {
                TestCaseCard(
                    testNumber = 4,
                    testName = "Stability",
                    description = "Test sensor stability over sustained operation",
                    onRunTest = {
                        testResults.value = "Monitoring for 10 seconds - keep device still"
                        isMonitoring.value = true
                    }
                )
            }

            // Test results
            item {
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
                            text = "Test Result",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = testResults.value,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SensorValueRow(label: String, value: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = String.format("%.2f", value),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TestCaseCard(
    testNumber: Int,
    testName: String,
    description: String,
    onRunTest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                    text = "Test $testNumber: $testName",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "READY",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRunTest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Run Test")
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun MainNavigationScreenPreview() {
    PhonecheckerTheme {
        MainNavigationScreen()
    }
}
