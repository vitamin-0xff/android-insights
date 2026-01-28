package com.example.phone_checker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.phone_checker.ui.features.apps.AppBehaviorScreen
import com.example.phone_checker.ui.features.audio.AudioScreen
import com.example.phone_checker.ui.features.battery.BatteryScreen
import com.example.phone_checker.ui.features.dashboard.DashboardScreen
import com.example.phone_checker.ui.features.insights.InsightsScreen
import com.example.phone_checker.ui.features.network.NetworkScreen
import com.example.phone_checker.ui.features.onboarding.OnboardingScreen
import com.example.phone_checker.ui.features.performance.PerformanceScreen
import com.example.phone_checker.ui.features.screen.ScreenHealthScreen
import com.example.phone_checker.ui.features.sensors.SensorsScreen
import com.example.phone_checker.ui.features.storage.StorageScreen
import com.example.phone_checker.ui.features.thermal.ThermalScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Onboarding.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onContinueClicked = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onMetricClick = { metricRoute ->
                    navController.navigate(metricRoute)
                }
            )
        }
        composable(Screen.Battery.route) {
            BatteryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Thermal.route) {
            ThermalScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Storage.route) {
            StorageScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Performance.route) {
            PerformanceScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Network.route) {
            NetworkScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.ScreenHealth.route) {
            ScreenHealthScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Sensors.route) {
            SensorsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Audio.route) {
            AudioScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.AppBehavior.route) {
            AppBehaviorScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Insights.route) {
            InsightsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
