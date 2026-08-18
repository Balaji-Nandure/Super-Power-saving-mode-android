package com.powersave.superpower

import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager
    private lateinit var rvAllowedApps: RecyclerView
    private lateinit var tvBatteryPercent: TextView
    private lateinit var tvPowerState: TextView
    private lateinit var btnTogglePower: Button
    private lateinit var btnPermissionSetup: Button
    private lateinit var btnConfigureApps: TextView
    private lateinit var btnExitMode: View
    private lateinit var cardEmergencyPhone: View

    private val selectAppsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadWhitelistedApps()
            }
        }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateBatteryDisplay()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefsManager = PreferencesManager(this)

        initViews()
        updateBatteryDisplay()
        loadWhitelistedApps()
        updatePowerSaveStateUi()

        if (prefsManager.isPowerSavingEnabled) {
            PowerManagerHelper.applySuperPowerSaving(this)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        updateBatteryDisplay()
        loadWhitelistedApps()
        updatePowerSaveStateUi()

        // Keep non-whitelisted background processes cleared
        if (prefsManager.isPowerSavingEnabled) {
            PowerManagerHelper.killAllBackgroundProcesses(this)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
    }

    // Samsung/Realme style: Pressing Back on Home Screen stays inside Super Power Saver
    override fun onBackPressed() {
        if (prefsManager.isPowerSavingEnabled) {
            // Do not exit on back press, stay in launcher
            return
        }
        super.onBackPressed()
    }

    private fun initViews() {
        tvBatteryPercent = findViewById(R.id.tvBatteryPercent)
        tvPowerState = findViewById(R.id.tvPowerState)
        btnTogglePower = findViewById(R.id.btnTogglePowerMode)
        btnPermissionSetup = findViewById(R.id.btnPermissionSetup)
        btnConfigureApps = findViewById(R.id.btnConfigureApps)
        btnExitMode = findViewById(R.id.btnExitMode)
        cardEmergencyPhone = findViewById(R.id.cardEmergencyPhone)
        rvAllowedApps = findViewById(R.id.rvAllowedApps)

        rvAllowedApps.layoutManager = LinearLayoutManager(this)

        // Emergency / Phone Dialer Action
        cardEmergencyPhone.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:")
            }
            startActivity(dialIntent)
        }

        // Configure 6 Whitelisted Apps
        btnConfigureApps.setOnClickListener {
            val intent = Intent(this, AppSelectionActivity::class.java)
            selectAppsLauncher.launch(intent)
        }

        // Exit Mode Button (Realme / Samsung Style)
        btnExitMode.setOnClickListener {
            showExitConfirmationDialog()
        }

        // Toggle Super Power Saving Mode
        btnTogglePower.setOnClickListener {
            val newState = !prefsManager.isPowerSavingEnabled
            prefsManager.isPowerSavingEnabled = newState
            if (newState) {
                PowerManagerHelper.applySuperPowerSaving(this)
                Toast.makeText(this, "⚡ Super Power Saving Mode ACTIVATED", Toast.LENGTH_SHORT).show()
            } else {
                PowerManagerHelper.restoreNormalSettings(this)
                Toast.makeText(this, "Super Power Saving Mode DEACTIVATED", Toast.LENGTH_SHORT).show()
            }
            updatePowerSaveStateUi()
        }

        // Open Permissions Setup Dialog
        btnPermissionSetup.setOnClickListener {
            showPermissionsDialog()
        }
    }

    private fun updateBatteryDisplay() {
        val pct = PowerManagerHelper.getBatteryPercentage(this)
        val isCharging = PowerManagerHelper.isCharging(this)

        if (pct >= 0) {
            tvBatteryPercent.text = if (isCharging) "$pct% (Charging)" else "$pct% Battery"
        } else {
            tvBatteryPercent.text = "Battery Active"
        }
    }

    private fun updatePowerSaveStateUi() {
        val isEnabled = prefsManager.isPowerSavingEnabled
        if (isEnabled) {
            tvPowerState.text = "10% Power Mode: ON"
            tvPowerState.setTextColor(ContextCompat.getColor(this, R.color.eco_green))
            btnTogglePower.text = "Disable Power Save"
            btnTogglePower.setBackgroundResource(R.drawable.bg_button_dark)
            btnTogglePower.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnExitMode.visibility = View.VISIBLE
        } else {
            tvPowerState.text = "10% Power Mode: OFF"
            tvPowerState.setTextColor(ContextCompat.getColor(this, R.color.power_orange))
            btnTogglePower.text = "Enable 10% Power"
            btnTogglePower.setBackgroundResource(R.drawable.bg_button_eco)
            btnTogglePower.setTextColor(ContextCompat.getColor(this, R.color.black))
            btnExitMode.visibility = View.GONE
        }
    }

    private fun loadWhitelistedApps() {
        val pm = packageManager
        val whitelistedPackages = prefsManager.getWhitelistedPackages()
        val allowedApps = mutableListOf<AppModel>()

        for (pkg in whitelistedPackages) {
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val appName = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(appInfo)
                allowedApps.add(AppModel(appName, pkg, icon, true))
            } catch (e: PackageManager.NameNotFoundException) {
                // App uninstalled or unavailable
            }
        }

        val adapter = LauncherAppsAdapter(this, allowedApps) {
            val intent = Intent(this, AppSelectionActivity::class.java)
            selectAppsLauncher.launch(intent)
        }
        rvAllowedApps.adapter = adapter
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(getString(R.string.dialog_exit_title))
            .setMessage(getString(R.string.dialog_exit_message))
            .setPositiveButton("Exit Mode") { _, _ ->
                prefsManager.isPowerSavingEnabled = false
                PowerManagerHelper.restoreNormalSettings(this)
                updatePowerSaveStateUi()
                // Prompt user to switch back to normal launcher
                PowerManagerHelper.promptSetDefaultLauncher(this)
                Toast.makeText(this, "Exited Super Power Saving Mode", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPermissionsDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_permissions)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnHomeLauncher = dialog.findViewById<Button>(R.id.btnSetDefaultHome)
        val tvHomeLauncherStatus = dialog.findViewById<TextView>(R.id.tvHomeLauncherStatus)
        val btnAccessibility = dialog.findViewById<Button>(R.id.btnGrantAccessibility)
        val tvAccessibilityStatus = dialog.findViewById<TextView>(R.id.tvAccessibilityStatus)
        val btnNotification = dialog.findViewById<Button>(R.id.btnGrantNotification)
        val tvNotificationStatus = dialog.findViewById<TextView>(R.id.tvNotificationStatus)
        val btnWriteSettings = dialog.findViewById<Button>(R.id.btnGrantWriteSettings)
        val tvWriteSettingsStatus = dialog.findViewById<TextView>(R.id.tvWriteSettingsStatus)
        val btnClose = dialog.findViewById<Button>(R.id.btnCloseDialog)

        // 1. Home Launcher Check
        btnHomeLauncher.setOnClickListener {
            PowerManagerHelper.promptSetDefaultLauncher(this)
            dialog.dismiss()
        }

        // 2. Kiosk Accessibility Check
        val hasAccessibility = PowerManagerHelper.isAccessibilityServiceEnabled(this)
        if (hasAccessibility) {
            btnAccessibility.text = "Active"
            btnAccessibility.isEnabled = false
            tvAccessibilityStatus.text = "✓ Kiosk Guard active (Blocks unauthorized apps)"
            tvAccessibilityStatus.setTextColor(ContextCompat.getColor(this, R.color.eco_green))
        } else {
            btnAccessibility.setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                dialog.dismiss()
            }
        }

        // 3. Notification Permission Check
        val hasNotif = PowerManagerHelper.isNotificationAccessGranted(this)
        if (hasNotif) {
            btnNotification.text = "Active"
            btnNotification.isEnabled = false
            tvNotificationStatus.text = "✓ Gatekeeper active (Only 6 apps allow notifications)"
            tvNotificationStatus.setTextColor(ContextCompat.getColor(this, R.color.eco_green))
        } else {
            btnNotification.setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                dialog.dismiss()
            }
        }

        // 4. Write Settings Permission Check
        val hasWrite = PowerManagerHelper.canWriteSystemSettings(this)
        if (hasWrite) {
            btnWriteSettings.text = "Active"
            btnWriteSettings.isEnabled = false
            tvWriteSettingsStatus.text = "✓ 15s timeout & screen dimming active"
            tvWriteSettingsStatus.setTextColor(ContextCompat.getColor(this, R.color.eco_green))
        } else {
            btnWriteSettings.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
                dialog.dismiss()
            }
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
