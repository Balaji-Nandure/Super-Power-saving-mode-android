package com.powersave.superpower

data class BatteryTelemetry(
    val percentage: Int,
    val isCharging: Boolean,
    val wattage: Double,
    val currentMa: Int,
    val voltageVolts: Double,
    val temperatureCelsius: Double,
    val chargerType: String,
    val chargeSpeedLabel: String,
    val timeRemainingMillis: Long,
    val health: String,
    val technology: String
)
