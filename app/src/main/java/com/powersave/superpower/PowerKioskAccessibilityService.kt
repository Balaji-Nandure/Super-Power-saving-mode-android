package com.powersave.superpower

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class PowerKioskAccessibilityService : AccessibilityService() {

    private lateinit var prefs: PreferencesManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = PreferencesManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (!prefs.isPowerSavingEnabled) return

            val packageName = event.packageName?.toString() ?: return

            // Allow our app, system UI, installer, and permission controllers
            if (packageName == this.packageName ||
                packageName == "com.android.systemui" ||
                packageName.contains("packageinstaller") ||
                packageName.contains("permissioncontroller") ||
                packageName.contains("settings")
            ) {
                return
            }

            // Check if package is allowed (Whitelisted 25 apps, Keyboards, or Phone)
            if (!prefs.isPackageAllowed(packageName)) {
                // Instantly block and redirect to Super Power Saver Home (Same as Samsung/Realme)
                val homeIntent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(homeIntent)
                Toast.makeText(this, "⚠️ App blocked in Super Power Saving Mode", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onInterrupt() {}
}
