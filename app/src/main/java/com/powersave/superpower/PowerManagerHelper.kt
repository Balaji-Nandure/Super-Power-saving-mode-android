package com.powersave.superpower

import android.app.Activity
import android.app.ActivityManager
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
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
     * Checks if Kiosk Accessibility Service is enabled.
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, PowerKioskAccessibilityService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(expectedComponentName)
    }

    /**
     * Kills non-essential background processes to free RAM and stop CPU polling.
     */
    fun killAllBackgroundProcesses(context: Context) {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return
            val pm = context.packageManager
            val prefs = PreferencesManager(context)
            val installedPackages = pm.getInstalledPackages(0)

            for (pkgInfo in installedPackages) {
                val pkgName = pkgInfo.packageName
                // Do not kill self or whitelisted packages
                if (pkgName != context.packageName && !prefs.isPackageAllowed(pkgName)) {
                    am.killBackgroundProcesses(pkgName)
                }
            }
            Log.d(TAG, "Background processes cleared.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to kill background processes: ${e.message}")
        }
    }

    /**
     * Prompts the user to set Super Power Saver as the Default Home Launcher.
     */
    fun promptSetDefaultLauncher(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = activity.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                    activity.startActivity(intent)
                    return
                }
            }
        }
        
        // Fallback for earlier versions or if role manager is not available
        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        } else {
            // General launcher picker intent
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activity.startActivity(Intent.createChooser(homeIntent, "Select Super Power Saver"))
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

        // 2. Kill heavy background processes
        killAllBackgroundProcesses(context)

        // 3. Clamp Screen Timeout to 15s and Dim Brightness if permission granted
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
