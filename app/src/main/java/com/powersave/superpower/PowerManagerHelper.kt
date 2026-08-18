package com.powersave.superpower

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

object PowerManagerHelper {

    private const val TAG = "PowerManagerHelper"
    private const val AGGRESSIVE_TIMEOUT_MS = 15000 // 15 seconds
    private const val ECO_BRIGHTNESS = 25 // Out of 255 (approx 10%)

    /**
     * Reads current battery percentage.
     */
    fun getBatteryPercentage(context: Context): Int {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, iFilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            -1
        }
    }

    /**
     * Checks if device is charging.
     */
    fun isCharging(context: Context): Boolean {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, iFilter)
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    /**
     * Checks if notification access is enabled.
     */
    fun isNotificationAccessGranted(context: Context): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return flat != null && flat.contains(context.packageName)
    }

    /**
     * Checks if WRITE_SETTINGS is granted.
     */
    fun canWriteSystemSettings(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }
    }

    /**
     * Applies maximum hardware power-saving optimizations.
     */
    fun applySuperPowerSaving(context: Context) {
        try {
            // 1. Disable Master Auto-Sync (Saves background network poll & wakelocks)
            ContentResolver.setMasterSyncAutomatically(false)
            Log.d(TAG, "Master auto-sync disabled.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable auto sync: ${e.message}")
        }

        // 2. Clamp Screen Timeout to 15s and Dim Brightness if permission granted
        if (canWriteSystemSettings(context)) {
            try {
                // Set screen off timeout to 15 seconds
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_OFF_TIMEOUT,
                    AGGRESSIVE_TIMEOUT_MS
                )
                // Set screen brightness to minimal ~10%
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    ECO_BRIGHTNESS
                )
                Log.d(TAG, "Display clamped to 15s timeout and minimal brightness.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to apply display power savings: ${e.message}")
            }
        }
    }

    /**
     * Restores normal settings when Super Power Saving is turned off.
     */
    fun restoreNormalSettings(context: Context) {
        try {
            ContentResolver.setMasterSyncAutomatically(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to re-enable sync: ${e.message}")
        }

        if (canWriteSystemSettings(context)) {
            try {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_OFF_TIMEOUT,
                    60000 // 1 minute
                )
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore display settings: ${e.message}")
            }
        }
    }
}
