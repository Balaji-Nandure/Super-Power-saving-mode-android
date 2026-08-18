package com.powersave.superpower

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import android.util.Log

object TurboChargeController {

    private const val TAG = "TurboChargeController"

    /**
     * Activates Turbo Fast Charging Mode:
     * 1. Terminates background tasks
     * 2. Shuts down master sync & network polling
     * 3. Dims display to 0% to prevent battery heat
     * 4. Keeps battery cool so phone's PMIC accepts maximum current without throttling
     */
    fun activateTurboCharging(context: Context) {
        try {
            // Disable sync to stop background networking
            ContentResolver.setMasterSyncAutomatically(false)
        } catch (e: Exception) {
            Log.e(TAG, "Sync error: ${e.message}")
        }

        // Kill non-essential background processes
        PowerManagerHelper.killAllBackgroundProcesses(context)

        // Drop screen brightness to 0% if permission granted to prevent heat
        if (PowerManagerHelper.canWriteSystemSettings(context)) {
            try {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    1 // Minimum 1/255
                )
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_OFF_TIMEOUT,
                    10000 // 10s timeout
                )
            } catch (e: Exception) {
                Log.e(TAG, "Brightness error: ${e.message}")
            }
        }
        Log.d(TAG, "Turbo Fast Charging Mode ACTIVATED. Thermal throttling minimized.")
    }

    /**
     * Deactivates Turbo Fast Charging Mode when unplugged.
     */
    fun deactivateTurboCharging(context: Context) {
        val prefs = PreferencesManager(context)
        if (prefs.isExtremeModeEnabled) {
            PowerManagerHelper.applyExtremeSurvivorProfile(context)
        } else if (prefs.isPowerSavingEnabled) {
            PowerManagerHelper.applySuperPowerSaving(context)
        } else {
            PowerManagerHelper.restoreNormalSettings(context)
        }
    }
}
