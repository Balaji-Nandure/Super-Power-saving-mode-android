package com.powersave.superpower

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationGatekeeperService : NotificationListenerService() {

    private lateinit var prefsManager: PreferencesManager

    override fun onCreate() {
        super.onCreate()
        prefsManager = PreferencesManager(this)
        Log.d(TAG, "Notification Gatekeeper Service active.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        // If Super Power Saving mode is OFF, do not intercept
        if (!prefsManager.isPowerSavingEnabled) {
            return
        }

        val packageName = sbn.packageName ?: return
        val notification = sbn.notification

        // 1. Always allow incoming Phone Calls, Alarms, and Telecom services
        if (isCriticalSystemNotification(sbn, notification)) {
            Log.d(TAG, "Allowed critical notification: $packageName")
            return
        }

        // 2. Check if the app is one of the 6 Whitelisted apps
        if (prefsManager.isPackageAllowed(packageName)) {
            Log.d(TAG, "Allowed whitelisted app notification: $packageName")
            return
        }

        // 3. Block and dismiss all non-whitelisted notifications to prevent CPU wakelocks and screen wakeups
        try {
            cancelNotification(sbn.key)
            Log.d(TAG, "Gatekeeper blocked and silenced notification from: $packageName")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling notification: ${e.message}")
        }
    }

    private fun isCriticalSystemNotification(sbn: StatusBarNotification, notification: Notification): Boolean {
        val packageName = sbn.packageName

        // Phone / Dialer / Telecom package check
        if (PreferencesManager.SYSTEM_CALL_PACKAGES.contains(packageName) ||
            packageName.contains("dialer") ||
            packageName.contains("telecom") ||
            packageName.contains("incallui")
        ) {
            return true
        }

        // Check if category is Call, Alarm, or Reminder
        if (notification.category == Notification.CATEGORY_CALL ||
            notification.category == Notification.CATEGORY_ALARM ||
            notification.category == Notification.CATEGORY_EVENT
        ) {
            return true
        }

        // Ongoing call foreground notifications
        if ((notification.flags and Notification.FLAG_ONGOING_EVENT) != 0 &&
            (notification.category == Notification.CATEGORY_CALL || packageName.contains("phone"))
        ) {
            return true
        }

        return false
    }

    companion object {
        private const val TAG = "NotificationGatekeeper"
    }
}
