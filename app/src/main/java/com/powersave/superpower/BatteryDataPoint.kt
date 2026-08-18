package com.powersave.superpower

data class BatteryDataPoint(
    val timestamp: Long,
    val level: Int,
    val isCharging: Boolean,
    val isScreenOn: Boolean
)
