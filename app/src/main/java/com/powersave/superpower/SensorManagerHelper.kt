package com.powersave.superpower

import android.content.Context
import android.provider.Settings
import android.util.Log

object SensorManagerHelper {

    private const val TAG = "SensorManagerHelper"

    /**
     * Freezes motion sensor polling & auto-rotation:
     * 1. Turns off Auto-Rotation (kills continuous 100Hz Gyroscope/Accelerometer computation).
     * 2. Eliminates pocket motion interrupts that wake the CPU during walking.
     */
    fun freezeSensors(context: Context) {
        if (PowerManagerHelper.canWriteSystemSettings(context)) {
            try {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION,
                    0 // Lock to portrait, kill Gyro/Accel polling
                )
                Log.d(TAG, "Sensors and Auto-Rotation successfully frozen.")
            } catch (e: Exception) {
                Log.e(TAG, "Error freezing sensor rotation: ${e.message}")
            }
        }
    }

    /**
     * Restores sensor rotation when exiting power saving mode.
     */
    fun restoreSensors(context: Context) {
        if (PowerManagerHelper.canWriteSystemSettings(context)) {
            try {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION,
                    1
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring sensor rotation: ${e.message}")
            }
        }
    }
}
