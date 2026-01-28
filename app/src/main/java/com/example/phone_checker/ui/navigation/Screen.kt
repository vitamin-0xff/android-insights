package com.example.phone_checker.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Battery : Screen("battery")
    object Thermal : Screen("thermal")
    object Storage : Screen("storage")
    object Performance : Screen("performance")
    object Network : Screen("network")
    object ScreenHealth : Screen("screen_health")
    object Sensors : Screen("sensors")
    object Audio : Screen("audio")
    object AppBehavior : Screen("app_behavior")
    object Insights : Screen("insights")
}
