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
        const val MAX_ALLOWED_APPS = 25

        // Hardware & Services Granular Power Toggles Keys
        private const val KEY_TOGGLE_INTERNET = "toggle_internet_kill"
        private const val KEY_TOGGLE_WIFI = "toggle_wifi_kill"
        private const val KEY_TOGGLE_BLUETOOTH = "toggle_bluetooth_kill"
        private const val KEY_TOGGLE_GPU = "toggle_gpu_optimizer"
        private const val KEY_TOGGLE_CPU_FREEZER = "toggle_cpu_freezer"
        private const val KEY_TOGGLE_REFRESH_RATE = "toggle_refresh_rate"
        private const val KEY_TOGGLE_GPS = "toggle_gps_disable"
        private const val KEY_TOGGLE_HAPTICS = "toggle_haptics_kill"
        private const val KEY_TOGGLE_SENSORS = "toggle_sensors_freeze"
        private const val KEY_TOGGLE_AUTO_SYNC = "toggle_auto_sync_kill"

        // Essential phone, SMS & Keyboard packages that are always whitelisted
        val SYSTEM_CALL_PACKAGES = setOf(
            "com.google.android.dialer",
            "com.android.dialer",
            "com.samsung.android.dialer",
            "com.android.server.telecom",
            "com.android.phone",
            "com.android.incallui",
            "com.google.android.apps.messaging",
            "com.android.mms",
            "com.samsung.android.messaging",
            // Keyboards & Input Methods (Google GBoard, Samsung, Vivo, SwiftKey)
            "com.google.android.inputmethod.latin",
            "com.android.inputmethod.latin",
            "com.samsung.android.honeyboard",
            "com.vivo.board",
            "com.baidu.input_vivo",
            "com.sohu.inputmethod.sogou.vivo",
            "com.iflytek.inputmethod.custom",
            "com.touchtype.swiftkey"
        )
    }

    var isPowerSavingEnabled: Boolean
        get() = prefs.getBoolean(KEY_POWER_SAVING_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_POWER_SAVING_ENABLED, value).apply()

    var isExtremeModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_EXTREME_MODE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_EXTREME_MODE_ENABLED, value).apply()

    // --- Granular Hardware & Service Toggles ---
    var isInternetKillEnabled: Boolean
        get() = prefs.getBoolean(KEY_TOGGLE_INTERNET, true)
        set(value) = prefs.edit().putBoolean(KEY_TOGGLE_INTERNET, value).apply()

    var isWifiKillEnabled: Boolean
        get() = prefs.getBoolean(KEY_TOGGLE_WIFI, true)
        set(value) = prefs.edit().putBoolean(KEY_TOGGLE_WIFI, value).apply()

    var isBluetoothKillEnabled: Boolean
        get() = prefs.getBoolean(KEY_TOGGLE_BLUETOOTH, true)
        set(value) = prefs.edit().putBoolean(KEY_TOGGLE_BLUETOOTH, value).apply()

    var isGpuOptimizerEnabled: Boolean
        get() = prefs.getBoolean(KEY_TOGGLE_GPU, true)
        set(value) = prefs.edit().putBoolean(KEY_TOGGLE_GPU, value).apply()

    var isCpuFreezerEnabled: Boolean
        get() = prefs.getBoolean(KEY_TOGGLE_CPU_FREEZER, true)
        set(value) = prefs.edit().putBoolean(KEY_TOGGLE_CPU_FREEZER, value).apply()

    var isRefreshRateThrottled: Boolean
        get() = prefs.getBoolean(KEY_TOGGLE_REFRESH_RATE, true)
        set(value) = prefs.edit().putBoolean(KEY_TOGGLE_REFRESH_RATE, value).apply()

    var isGpsDisabled: Boolean
        get() = prefs.getBoolean(KEY_TOGGLE_GPS, true)
        set(value) = prefs.edit().putBoolean(KEY_TOGGLE_GPS, value).apply()

    var isHapticsDisabled: Boolean
        get() = prefs.getBoolean(KEY_TOGGLE_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_TOGGLE_HAPTICS, value).apply()

    var isSensorsFrozen: Boolean
        get() = prefs.getBoolean(KEY_TOGGLE_SENSORS, true)
        set(value) = prefs.edit().putBoolean(KEY_TOGGLE_SENSORS, value).apply()

    var isAutoSyncDisabled: Boolean
        get() = prefs.getBoolean(KEY_TOGGLE_AUTO_SYNC, true)
        set(value) = prefs.edit().putBoolean(KEY_TOGGLE_AUTO_SYNC, value).apply()

    fun getActiveServiceThrottledCount(): Int {
        var count = 0
        if (isInternetKillEnabled) count++
        if (isWifiKillEnabled) count++
        if (isBluetoothKillEnabled) count++
        if (isGpuOptimizerEnabled) count++
        if (isCpuFreezerEnabled) count++
        if (isRefreshRateThrottled) count++
        if (isGpsDisabled) count++
        if (isHapticsDisabled) count++
        if (isSensorsFrozen) count++
        if (isAutoSyncDisabled) count++
        return count
    }

    fun getTotalServicesCount(): Int = 10

    fun getEstimatedSavingsPercentage(): Int {
        val active = getActiveServiceThrottledCount()
        // Baseline scale: 10/10 active gives ~80% savings, scaling down to 0%
        return ((active / 10.0) * 80).toInt()
    }

    fun setAllServices(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_TOGGLE_INTERNET, enabled)
            .putBoolean(KEY_TOGGLE_WIFI, enabled)
            .putBoolean(KEY_TOGGLE_BLUETOOTH, enabled)
            .putBoolean(KEY_TOGGLE_GPU, enabled)
            .putBoolean(KEY_TOGGLE_CPU_FREEZER, enabled)
            .putBoolean(KEY_TOGGLE_REFRESH_RATE, enabled)
            .putBoolean(KEY_TOGGLE_GPS, enabled)
            .putBoolean(KEY_TOGGLE_HAPTICS, enabled)
            .putBoolean(KEY_TOGGLE_SENSORS, enabled)
            .putBoolean(KEY_TOGGLE_AUTO_SYNC, enabled)
            .apply()
    }

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
        // System phone/calls, SMS & Keyboards (Gboard, etc.) are ALWAYS allowed
        if (SYSTEM_CALL_PACKAGES.contains(packageName) ||
            packageName.contains("dialer") ||
            packageName.contains("telecom") ||
            packageName.contains("incallui") ||
            packageName.contains("messaging") ||
            packageName.contains("mms") ||
            packageName.contains("inputmethod") ||
            packageName.contains("keyboard") ||
            packageName.contains("honeyboard") ||
            packageName.contains("latin") ||
            packageName.contains("swiftkey") ||
            packageName.contains("gboard")
        ) {
            return true
        }

        // In 1% Extreme Blackout Mode: ONLY Phone, SMS, and Keyboards are allowed!
        if (isExtremeModeEnabled) {
            return false
        }

        // In Super Power Saving Mode: Check up to 25 Whitelisted apps
        val whitelisted = getWhitelistedPackages()
        return whitelisted.contains(packageName)
    }
}
