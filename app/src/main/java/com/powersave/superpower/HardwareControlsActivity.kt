package com.powersave.superpower

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class HardwareControlsActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager

    // HUD Views
    private lateinit var tvActiveCount: TextView
    private lateinit var tvSavingsPercent: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnResetDefaults: Button
    private lateinit var btnApplyAll: Button

    // 10 Granular Service Switches
    private lateinit var switchInternet: SwitchMaterial
    private lateinit var switchWifi: SwitchMaterial
    private lateinit var switchBluetooth: SwitchMaterial
    private lateinit var switchGps: SwitchMaterial
    private lateinit var switchCpu: SwitchMaterial
    private lateinit var switchGpu: SwitchMaterial
    private lateinit var switchRefreshRate: SwitchMaterial
    private lateinit var switchSensors: SwitchMaterial
    private lateinit var switchHaptics: SwitchMaterial
    private lateinit var switchSync: SwitchMaterial

    // Vivo Special Quick Launcher Views
    private lateinit var btnVivoImanager: View
    private lateinit var btnVivoAutostart: View
    private lateinit var btnVivo5G: View
    private lateinit var btnVivoResolution: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hardware_controls)

        prefsManager = PreferencesManager(this)

        initViews()
        loadSwitchStates()
        setupListeners()
        updateHudStats()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvActiveCount = findViewById(R.id.tvActiveCount)
        tvSavingsPercent = findViewById(R.id.tvSavingsPercent)
        btnResetDefaults = findViewById(R.id.btnResetDefaults)
        btnApplyAll = findViewById(R.id.btnApplyAll)

        // Switches
        switchInternet = findViewById(R.id.switchInternet)
        switchWifi = findViewById(R.id.switchWifi)
        switchBluetooth = findViewById(R.id.switchBluetooth)
        switchGps = findViewById(R.id.switchGps)
        switchCpu = findViewById(R.id.switchCpu)
        switchGpu = findViewById(R.id.switchGpu)
        switchRefreshRate = findViewById(R.id.switchRefreshRate)
        switchSensors = findViewById(R.id.switchSensors)
        switchHaptics = findViewById(R.id.switchHaptics)
        switchSync = findViewById(R.id.switchSync)

        // Vivo Shortcuts
        btnVivoImanager = findViewById(R.id.btnVivoImanager)
        btnVivoAutostart = findViewById(R.id.btnVivoAutostart)
        btnVivo5G = findViewById(R.id.btnVivo5G)
        btnVivoResolution = findViewById(R.id.btnVivoResolution)
    }

    private fun loadSwitchStates() {
        switchInternet.isChecked = prefsManager.isInternetKillEnabled
        switchWifi.isChecked = prefsManager.isWifiKillEnabled
        switchBluetooth.isChecked = prefsManager.isBluetoothKillEnabled
        switchGps.isChecked = prefsManager.isGpsDisabled
        switchCpu.isChecked = prefsManager.isCpuFreezerEnabled
        switchGpu.isChecked = prefsManager.isGpuOptimizerEnabled
        switchRefreshRate.isChecked = prefsManager.isRefreshRateThrottled
        switchSensors.isChecked = prefsManager.isSensorsFrozen
        switchHaptics.isChecked = prefsManager.isHapticsDisabled
        switchSync.isChecked = prefsManager.isAutoSyncDisabled
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        // Toggles Change Listeners
        switchInternet.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isInternetKillEnabled = isChecked
            updateHudStats()
            if (isChecked) {
                HardwareServiceManager.openMobileDataSettings(this)
            }
        }

        switchWifi.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isWifiKillEnabled = isChecked
            updateHudStats()
            if (isChecked) {
                HardwareServiceManager.openWifiSettings(this)
            }
        }

        switchBluetooth.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isBluetoothKillEnabled = isChecked
            updateHudStats()
            if (isChecked) {
                HardwareServiceManager.openBluetoothSettings(this)
            }
        }

        switchGps.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isGpsDisabled = isChecked
            updateHudStats()
            if (isChecked) {
                HardwareServiceManager.openLocationSettings(this)
            }
        }

        switchCpu.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isCpuFreezerEnabled = isChecked
            updateHudStats()
            if (isChecked && prefsManager.isPowerSavingEnabled) {
                HardwareServiceManager.applyCpuFreezer(this)
            }
        }

        switchGpu.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isGpuOptimizerEnabled = isChecked
            updateHudStats()
            if (prefsManager.isPowerSavingEnabled) {
                HardwareServiceManager.applyGpuOptimization(this, isChecked)
            }
        }

        switchRefreshRate.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isRefreshRateThrottled = isChecked
            updateHudStats()
            if (isChecked) {
                HardwareServiceManager.applyRefreshRateClamp(this, true)
                HardwareServiceManager.openDisplaySettings(this)
            } else {
                HardwareServiceManager.applyRefreshRateClamp(this, false)
            }
        }

        switchSensors.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isSensorsFrozen = isChecked
            updateHudStats()
            if (prefsManager.isPowerSavingEnabled) {
                HardwareServiceManager.applySensors(this, isChecked)
            }
        }

        switchHaptics.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isHapticsDisabled = isChecked
            updateHudStats()
            if (prefsManager.isPowerSavingEnabled) {
                HardwareServiceManager.applyHaptics(this, isChecked)
            }
        }

        switchSync.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isAutoSyncDisabled = isChecked
            updateHudStats()
            if (prefsManager.isPowerSavingEnabled) {
                HardwareServiceManager.applyAutoSync(this, isChecked)
            }
        }

        // Vivo OS Specific Shortcuts
        btnVivoImanager.setOnClickListener {
            val opened = HardwareServiceManager.openVivoBackgroundPowerSettings(this)
            if (!opened) {
                Toast.makeText(this, "Opening Battery Settings...", Toast.LENGTH_SHORT).show()
            }
        }

        btnVivoAutostart.setOnClickListener {
            val opened = HardwareServiceManager.openVivoAutostartSettings(this)
            if (!opened) {
                Toast.makeText(this, "Opening App Settings...", Toast.LENGTH_SHORT).show()
            }
        }

        btnVivo5G.setOnClickListener {
            HardwareServiceManager.openVivo5GSettings(this)
        }

        btnVivoResolution.setOnClickListener {
            HardwareServiceManager.openDisplaySettings(this)
        }

        // Bottom Action Buttons
        btnApplyAll.setOnClickListener {
            prefsManager.setAllServices(true)
            loadSwitchStates()
            updateHudStats()
            if (prefsManager.isPowerSavingEnabled) {
                PowerManagerHelper.applySuperPowerSaving(this)
            }
            Toast.makeText(this, getString(R.string.toast_all_services_enabled), Toast.LENGTH_SHORT).show()
        }

        btnResetDefaults.setOnClickListener {
            prefsManager.setAllServices(true)
            loadSwitchStates()
            updateHudStats()
            Toast.makeText(this, getString(R.string.toast_services_reset), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateHudStats() {
        val active = prefsManager.getActiveServiceThrottledCount()
        val total = prefsManager.getTotalServicesCount()
        val savings = prefsManager.getEstimatedSavingsPercentage()

        tvActiveCount.text = "$active/$total Services Throttled"
        tvSavingsPercent.text = "~$savings% Saved"

        if (active >= 8) {
            tvActiveCount.setTextColor(ContextCompat.getColor(this, R.color.eco_green))
            tvSavingsPercent.setTextColor(ContextCompat.getColor(this, R.color.eco_green))
        } else if (active >= 4) {
            tvActiveCount.setTextColor(ContextCompat.getColor(this, R.color.power_orange))
            tvSavingsPercent.setTextColor(ContextCompat.getColor(this, R.color.power_orange))
        } else {
            tvActiveCount.setTextColor(ContextCompat.getColor(this, R.color.power_red))
            tvSavingsPercent.setTextColor(ContextCompat.getColor(this, R.color.power_red))
        }
    }
}
