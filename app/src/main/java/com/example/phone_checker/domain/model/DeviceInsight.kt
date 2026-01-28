package com.example.phone_checker.domain.model

data class DeviceInsight(
    val id: String,
    val category: InsightCategory,
    val severity: InsightSeverity,
    val title: String,
    val description: String,
    val recommendation: String,
    val actionable: Boolean = true
)

enum class InsightCategory {
    BATTERY, THERMAL, STORAGE, PERFORMANCE, SYSTEM
}

enum class InsightSeverity {
    INFO, WARNING, CRITICAL, POSITIVE
}
