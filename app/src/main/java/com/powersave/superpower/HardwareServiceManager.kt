package com.powersave.superpower

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log

object HardwareServiceManager {

    private const val TAG = "HardwareServiceManager"

    // --- 1. AUTO-SYNC KILLER ---
    fun applyAutoSync(context: Context, killSync: Boolean) {
        try {
            ContentResolver.setMasterSyncAutomatically(!killSync)
            Log.d(TAG, "Master AutoSync set to: ${!killSync}")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying AutoSync: ${e.message}")
        }
    }

    // --- 2. HAPTIC & VIBRATION MOTOR CONTROLLER ---
    fun applyHaptics(context: Context, disableHaptics: Boolean) {
        if (PowerManagerHelper.canWriteSystemSettings(context)) {
            try {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.HAPTIC_FEEDBACK_ENABLED,
                    if (disableHaptics) 0 else 1
                )
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.VIBRATE_WHEN_RINGING,
                    if (disableHaptics) 0 else 1
                )
                Log.d(TAG, "Haptics set to: ${!disableHaptics}")
            } catch (e: Exception) {
                Log.e(TAG, "Error applying Haptics: ${e.message}")
            }
        }
    }

    // --- 3. MOTION & STEP SENSORS CONTROLLER ---
    fun applySensors(context: Context, freeze: Boolean) {
        if (freeze) {
            SensorManagerHelper.freezeSensors(context)
        } else {
            SensorManagerHelper.restoreSensors(context)
        }
    }

    // --- 4. GPU ENGINE & ANIMATION SCALING OPTIMIZER ---
    fun applyGpuOptimization(context: Context, optimize: Boolean) {
        if (PowerManagerHelper.canWriteSystemSettings(context)) {
            try {
                val scale = if (optimize) 0.0f else 1.0f
                Settings.Global.putFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, scale)
                Settings.Global.putFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, scale)
                Settings.Global.putFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, scale)
                Log.d(TAG, "GPU Animation scale set to: $scale")
            } catch (e: Exception) {
                // Settings.Global requires WRITE_SECURE_SETTINGS or system privilege on some ROMs
                Log.d(TAG, "Notice: GPU global animation scale needs secure settings: ${e.message}")
            }
        }
    }

    // --- 5. CPU FREEZER & BACKGROUND PROCESS KILLER ---
    fun applyCpuFreezer(context: Context) {
        PowerManagerHelper.killAllBackgroundProcesses(context)
    }

    // --- 6. DISPLAY REFRESH RATE THROTTLE (120Hz -> 60Hz) ---
    fun applyRefreshRateClamp(context: Context, clampTo60Hz: Boolean) {
        if (PowerManagerHelper.canWriteSystemSettings(context)) {
            try {
                val targetRate = if (clampTo60Hz) 60.0f else 120.0f
                Settings.System.putFloat(context.contentResolver, "peak_refresh_rate", targetRate)
                Settings.System.putFloat(context.contentResolver, "min_refresh_rate", 60.0f)
                Log.d(TAG, "Peak refresh rate clamped to: $targetRate")
            } catch (e: Exception) {
                Log.d(TAG, "Notice: Peak refresh rate requires system privilege on this vendor: ${e.message}")
            }
        }
    }

    // --- 7. QUICK INTENT LAUNCHERS FOR RESTRICTED SYSTEM TOGGLES ---

    /**
     * Opens Cellular Data / Network panel
     */
    fun openMobileDataSettings(activity: Activity) {
        val intents = mutableListOf<Intent>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intents.add(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
        }
        intents.add(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS))
        intents.add(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        intents.add(Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS))

        for (intent in intents) {
            try {
                if (intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                    return
                }
            } catch (e: Exception) {
                // try next
            }
        }
        activity.startActivity(Intent(Settings.ACTION_SETTINGS))
    }

    /**
     * Opens Wi-Fi / Wi-Fi Scanning Settings Panel
     */
    fun openWifiSettings(activity: Activity) {
        val intents = mutableListOf<Intent>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intents.add(Intent(Settings.Panel.ACTION_WIFI))
        }
        intents.add(Intent(Settings.ACTION_WIFI_SETTINGS))
        intents.add(Intent(Settings.ACTION_WIFI_IP_SETTINGS))

        for (intent in intents) {
            try {
                if (intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                    return
                }
            } catch (e: Exception) {
                // try next
            }
        }
        activity.startActivity(Intent(Settings.ACTION_SETTINGS))
    }

    /**
     * Opens Bluetooth Settings
     */
    fun openBluetoothSettings(activity: Activity) {
        val intents = listOf(
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
            Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        )
        for (intent in intents) {
            try {
                if (intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                    return
                }
            } catch (e: Exception) {
                // try next
            }
        }
        activity.startActivity(Intent(Settings.ACTION_SETTINGS))
    }

    /**
     * Opens Location / GPS Settings
     */
    fun openLocationSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        } else {
            activity.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    /**
     * Opens Display Settings (Screen Resolution WQHD+ vs FHD+, 60Hz vs 120Hz)
     */
    fun openDisplaySettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        } else {
            activity.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    // --- 8. VIVO X70 PRO+ & FUNTOUCH OS DEEP-LINK POWER OPTIMIZATION ---

    /**
     * Opens Vivo iManager -> High Background Power Consumption Whitelist
     */
    fun openVivoBackgroundPowerSettings(activity: Activity): Boolean {
        val vivoIntents = listOf(
            // Vivo iManager Background Power Usage
            Intent().setComponent(
                ComponentName("com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity")
            ),
            Intent().setComponent(
                ComponentName("com.iqoo.powersaving", "com.iqoo.powersaving.PowerSavingManagerActivity")
            ),
            Intent().setComponent(
                ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
            ),
            Intent().setComponent(
                ComponentName("com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.UnifiedPowerModelActivity")
            ),
            Intent().setComponent(
                ComponentName("com.iqoo.secure", "com.iqoo.secure.MainguideActivity")
            ),
            Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
        )

        for (intent in vivoIntents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                    return true
                }
            } catch (e: Exception) {
                // try next
            }
        }
        return false
    }

    /**
     * Opens Vivo Autostart & App Launch Manager
     */
    fun openVivoAutostartSettings(activity: Activity): Boolean {
        val autostartIntents = listOf(
            Intent().setComponent(
                ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.PurviewTabActivity")
            ),
            Intent().setComponent(
                ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity")
            ),
            Intent().setComponent(
                ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")
            ),
            Intent().setComponent(
                ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")
            ),
            Intent(Settings.ACTION_APPLICATION_SETTINGS)
        )

        for (intent in autostartIntents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                    return true
                }
            } catch (e: Exception) {
                // try next
            }
        }
        return false
    }

    /**
     * Opens Vivo Cellular & 5G / 4G Mode Switch
     */
    fun openVivo5GSettings(activity: Activity) {
        val intents = listOf(
            Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS),
            Intent(Settings.ACTION_DATA_ROAMING_SETTINGS),
            Intent(Settings.ACTION_WIRELESS_SETTINGS)
        )
        for (intent in intents) {
            try {
                if (intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                    return
                }
            } catch (e: Exception) {
                // try next
            }
        }
        activity.startActivity(Intent(Settings.ACTION_SETTINGS))
    }
}
