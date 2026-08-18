package com.powersave.superpower

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "super_power_save_prefs"
        private const val KEY_WHITELISTED_APPS = "whitelisted_apps"
        private const val KEY_POWER_SAVING_ENABLED = "power_saving_enabled"
        private const val KEY_EXTREME_MODE_ENABLED = "extreme_mode_enabled"
        const val MAX_ALLOWED_APPS = 6

        // Essential phone & SMS packages that are always whitelisted
        val SYSTEM_CALL_PACKAGES = setOf(
            "com.google.android.dialer",
            "com.android.dialer",
            "com.samsung.android.dialer",
            "com.android.server.telecom",
            "com.android.phone",
            "com.android.incallui",
            "com.google.android.apps.messaging",
            "com.android.mms",
            "com.samsung.android.messaging"
        )
    }

    var isPowerSavingEnabled: Boolean
        get() = prefs.getBoolean(KEY_POWER_SAVING_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_POWER_SAVING_ENABLED, value).apply()

    var isExtremeModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_EXTREME_MODE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_EXTREME_MODE_ENABLED, value).apply()

    fun getWhitelistedPackages(): Set<String> {
        return prefs.getStringSet(KEY_WHITELISTED_APPS, emptySet()) ?: emptySet()
    }

    fun saveWhitelistedPackages(packages: Set<String>): Boolean {
        if (packages.size > MAX_ALLOWED_APPS) {
            return false
        }
        prefs.edit().putStringSet(KEY_WHITELISTED_APPS, packages).apply()
        return true
    }

    fun isPackageAllowed(packageName: String): Boolean {
        // System phone/calls and SMS are ALWAYS allowed
        if (SYSTEM_CALL_PACKAGES.contains(packageName) ||
            packageName.contains("dialer") ||
            packageName.contains("telecom") ||
            packageName.contains("incallui") ||
            packageName.contains("messaging") ||
            packageName.contains("mms")
        ) {
            return true
        }

        // In 1% Extreme Blackout Mode: ONLY Phone and SMS are allowed!
        if (isExtremeModeEnabled) {
            return false
        }

        // In 10% Super Mode: Check 6 Whitelisted apps
        val whitelisted = getWhitelistedPackages()
        return whitelisted.contains(packageName)
    }
}
