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
        const val MAX_ALLOWED_APPS = 6

        // Essential system packages that are always whitelisted (e.g. Telecom/Dialer/Android System)
        val SYSTEM_CALL_PACKAGES = setOf(
            "com.google.android.dialer",
            "com.android.dialer",
            "com.samsung.android.dialer",
            "com.android.server.telecom",
            "com.android.phone",
            "com.android.incallui",
            "com.google.android.apps.messaging",
            "com.android.mms"
        )
    }

    var isPowerSavingEnabled: Boolean
        get() = prefs.getBoolean(KEY_POWER_SAVING_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_POWER_SAVING_ENABLED, value).apply()

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
        // System phone/calls are always allowed
        if (SYSTEM_CALL_PACKAGES.contains(packageName) || packageName.contains("dialer") || packageName.contains("telecom") || packageName.contains("incallui")) {
            return true
        }
        val whitelisted = getWhitelistedPackages()
        return whitelisted.contains(packageName)
    }
}
