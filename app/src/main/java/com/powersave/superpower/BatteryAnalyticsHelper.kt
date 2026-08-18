package com.powersave.superpower

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import kotlin.math.abs

object BatteryAnalyticsHelper {

    fun getRealTimeTelemetry(context: Context): BatteryTelemetry {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, iFilter)
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

        // 1. Percentage
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percentage = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 0

        // 2. Charging Status & Plugged Type
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val plugged = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val chargerType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Adapter"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Qi Charger"
            else -> "On Battery"
        }

        // 3. Voltage (mV -> V)
        val voltageMv = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000) ?: 4000
        val voltageVolts = voltageMv / 1000.0

        // 4. Current (Microamperes -> Milliamperes)
        var rawCurrentUa = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0
        if (rawCurrentUa == Int.MIN_VALUE || rawCurrentUa == 0) {
            // Fallback estimation if kernel property is masked
            rawCurrentUa = if (isCharging) 2000000 else -250000
        }

        var currentMa = rawCurrentUa / 1000
        // Standardize: Positive when charging, negative when discharging
        if (isCharging && currentMa < 0) {
            currentMa = abs(currentMa)
        } else if (!isCharging && currentMa > 0) {
            currentMa = -abs(currentMa)
        }

        // 5. Wattage Calculation (Watts = Volts * Amps)
        val absCurrentAmps = abs(currentMa) / 1000.0
        val wattage = voltageVolts * absCurrentAmps

        // 6. Charging Speed Label
        val chargeSpeedLabel = if (isCharging) {
            when {
                wattage >= 45.0 -> "⚡ HyperCharge / SuperVOOC (${String.format("%.1f", wattage)}W)"
                wattage >= 25.0 -> "⚡ Super Fast Charging (${String.format("%.1f", wattage)}W)"
                wattage >= 15.0 -> "⚡ Quick Fast Charging (${String.format("%.1f", wattage)}W)"
                wattage >= 8.0 -> "⚡ Standard Charging (${String.format("%.1f", wattage)}W)"
                else -> "⚡ Slow Charging (${String.format("%.1f", wattage)}W)"
            }
        } else {
            when {
                wattage <= 0.5 -> "🟢 Ultra-Low Drain (${String.format("%.2f", wattage)}W)"
                wattage <= 1.2 -> "🟢 Optimal Eco Drain (${String.format("%.2f", wattage)}W)"
                else -> "🟡 Moderate Drain (${String.format("%.2f", wattage)}W)"
            }
        }

        // 7. Temperature (Tenths of degree -> Celsius)
        val tempRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 300) ?: 300
        val temperatureCelsius = tempRaw / 10.0

        // 8. Time Remaining to Full Charge
        var timeRemainingMillis: Long = -1
        if (isCharging) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && batteryManager != null) {
                try {
                    timeRemainingMillis = batteryManager.computeChargeTimeRemaining()
                } catch (e: Exception) {
                    timeRemainingMillis = -1
                }
            }
            if (timeRemainingMillis <= 0 && currentMa > 0) {
                // Intelligent mathematical fallback calculation
                val remainingCapacityPct = (100 - percentage).coerceAtLeast(0)
                // Assume standard ~4500mAh nominal capacity
                val remainingMah = (4500 * (remainingCapacityPct / 100.0))
                val hoursToFull = remainingMah / currentMa.toDouble()
                timeRemainingMillis = (hoursToFull * 3600 * 1000).toLong()
            }
        }

        // 9. Battery Health
        val healthRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
        val health = when (healthRaw) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat (Warm)"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Degraded"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            else -> "Normal"
        }

        // 10. Technology
        val technology = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        return BatteryTelemetry(
            percentage = percentage,
            isCharging = isCharging,
            wattage = wattage,
            currentMa = currentMa,
            voltageVolts = voltageVolts,
            temperatureCelsius = temperatureCelsius,
            chargerType = chargerType,
            chargeSpeedLabel = chargeSpeedLabel,
            timeRemainingMillis = timeRemainingMillis,
            health = health,
            technology = technology
        )
    }

    fun formatDuration(millis: Long): String {
        if (millis <= 0) return "--"
        val totalMinutes = millis / (1000 * 60)
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return if (hours > 0) {
            "${hours}h ${mins}m"
        } else {
            "${mins} mins"
        }
    }
}
