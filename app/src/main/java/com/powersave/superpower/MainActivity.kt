package com.powersave.superpower

import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager
    private lateinit var historyManager: BatteryHistoryManager
    private lateinit var rootLayout: View
    private lateinit var rvAllowedApps: RecyclerView
    private lateinit var tvBatteryPercent: TextView
    private lateinit var tvPowerState: TextView
    private lateinit var btnTogglePower: Button
    private lateinit var btnPermissionSetup: Button
    private lateinit var btnConfigureApps: TextView
    private lateinit var btnExitMode: View
    private lateinit var cardEmergencyPhone: View
    private lateinit var cardEmergencySms: View
    private lateinit var cardExtremeModeBanner: View
    private lateinit var tvExtremeTitle: TextView
    private lateinit var tvExtremeSub: TextView
    private lateinit var btnToggleExtreme: Button
    private lateinit var sectionAppsHeader: View

    // ⚡ Telemetry HUD Views
    private lateinit var cardBatteryHud: View
    private lateinit var ivChargingIcon: ImageView
    private lateinit var tvChargingHeadline: TextView
    private lateinit var tvWattageVal: TextView
    private lateinit var tvCurrentVal: TextView
    private lateinit var tvVoltageVal: TextView
    private lateinit var tvTempVal: TextView
    private lateinit var tvChargerTypeVal: TextView
    private lateinit var tvTimeToFullVal: TextView

    private val telemetryHandler = Handler(Looper.getMainLooper())
    private val telemetryRunnable = object : Runnable {
        override fun run() {
            updateBatteryTelemetry()
            telemetryHandler.postDelayed(this, 1500)
        }
    }

    private val selectAppsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadWhitelistedApps()
            }
        }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateBatteryTelemetry()
            logBatteryPoint()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefsManager = PreferencesManager(this)
        historyManager = BatteryHistoryManager(this)

        initViews()
        updateBatteryTelemetry()
        loadWhitelistedApps()
        updatePowerSaveStateUi()
        logBatteryPoint()

        if (prefsManager.isExtremeModeEnabled) {
            PowerManagerHelper.applyExtremeSurvivorProfile(this)
            applyMonochromeGrayscale(true)
        } else if (prefsManager.isPowerSavingEnabled) {
            PowerManagerHelper.applySuperPowerSaving(this)
            applyMonochromeGrayscale(false)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        telemetryHandler.post(telemetryRunnable)
        loadWhitelistedApps()
        updatePowerSaveStateUi()
        logBatteryPoint()

        if (prefsManager.isPowerSavingEnabled || prefsManager.isExtremeModeEnabled) {
            PowerManagerHelper.killAllBackgroundProcesses(this)
            PowerManagerHelper.ensureRingtoneAudible(this)
        }
    }

    override fun onPause() {
        super.onPause()
        telemetryHandler.removeCallbacks(telemetryRunnable)
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
    }

    override fun onBackPressed() {
        if (prefsManager.isPowerSavingEnabled || prefsManager.isExtremeModeEnabled) {
            return
        }
        super.onBackPressed()
    }

    private fun initViews() {
        rootLayout = findViewById(R.id.rootLayout)
        tvBatteryPercent = findViewById(R.id.tvBatteryPercent)
        tvPowerState = findViewById(R.id.tvPowerState)
        btnTogglePower = findViewById(R.id.btnTogglePowerMode)
        btnPermissionSetup = findViewById(R.id.btnPermissionSetup)
        btnConfigureApps = findViewById(R.id.btnConfigureApps)
        btnExitMode = findViewById(R.id.btnExitMode)
        cardEmergencyPhone = findViewById(R.id.cardEmergencyPhone)
        cardEmergencySms = findViewById(R.id.cardEmergencySms)
        cardExtremeModeBanner = findViewById(R.id.cardExtremeModeBanner)
        tvExtremeTitle = findViewById(R.id.tvExtremeTitle)
        tvExtremeSub = findViewById(R.id.tvExtremeSub)
        btnToggleExtreme = findViewById(R.id.btnToggleExtreme)
        sectionAppsHeader = findViewById(R.id.sectionAppsHeader)
        rvAllowedApps = findViewById(R.id.rvAllowedApps)

        cardBatteryHud = findViewById(R.id.cardBatteryHud)
        ivChargingIcon = findViewById(R.id.ivChargingIcon)
        tvChargingHeadline = findViewById(R.id.tvChargingHeadline)
        tvWattageVal = findViewById(R.id.tvWattageVal)
        tvCurrentVal = findViewById(R.id.tvCurrentVal)
        tvVoltageVal = findViewById(R.id.tvVoltageVal)
        tvTempVal = findViewById(R.id.tvTempVal)
        tvChargerTypeVal = findViewById(R.id.tvChargerTypeVal)
        tvTimeToFullVal = findViewById(R.id.tvTimeToFullVal)

        rvAllowedApps.layoutManager = LinearLayoutManager(this)

        cardBatteryHud.isClickable = true
        cardBatteryHud.isFocusable = true
        cardBatteryHud.setOnClickListener {
            val intent = Intent(this, AnalyticsActivity::class.java)
            startActivity(intent)
        }

        cardEmergencyPhone.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:")
            }
            startActivity(dialIntent)
        }

        cardEmergencySms.setOnClickListener {
            val smsIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MESSAGING)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (smsIntent.resolveActivity(packageManager) != null) {
                startActivity(smsIntent)
            } else {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                startActivity(fallbackIntent)
            }
        }

        btnConfigureApps.setOnClickListener {
            val intent = Intent(this, AppSelectionActivity::class.java)
            selectAppsLauncher.launch(intent)
        }

        btnExitMode.setOnClickListener {
            showExitConfirmationDialog()
        }

        btnToggleExtreme.setOnClickListener {
            if (prefsManager.isExtremeModeEnabled) {
                prefsManager.isExtremeModeEnabled = false
                prefsManager.isPowerSavingEnabled = true
                PowerManagerHelper.applySuperPowerSaving(this)
                applyMonochromeGrayscale(false)
                Toast.makeText(this, "Switched to 10% Super Mode (6 Apps Active)", Toast.LENGTH_SHORT).show()
                updatePowerSaveStateUi()
            } else {
                showExtremeModeConfirmationDialog()
            }
        }

        btnTogglePower.setOnClickListener {
            if (prefsManager.isExtremeModeEnabled) {
                prefsManager.isExtremeModeEnabled = false
            }
            val newState = !prefsManager.isPowerSavingEnabled
            prefsManager.isPowerSavingEnabled = newState
            if (newState) {
                PowerManagerHelper.applySuperPowerSaving(this)
                applyMonochromeGrayscale(false)
                Toast.makeText(this, "⚡ 10% Super Power Mode ACTIVATED", Toast.LENGTH_SHORT).show()
            } else {
                PowerManagerHelper.restoreNormalSettings(this)
                applyMonochromeGrayscale(false)
                Toast.makeText(this, "Power Saving Mode DEACTIVATED", Toast.LENGTH_SHORT).show()
            }
            updatePowerSaveStateUi()
        }

        btnPermissionSetup.setOnClickListener {
            showPermissionsDialog()
        }
    }

    private fun logBatteryPoint() {
        val pct = PowerManagerHelper.getBatteryPercentage(this)
        val isCharging = PowerManagerHelper.isCharging(this)
        historyManager.recordDataPoint(pct, isCharging, true)
    }

    private fun updateBatteryTelemetry() {
        val t = BatteryAnalyticsHelper.getRealTimeTelemetry(this)

        tvBatteryPercent.text = "${t.percentage}%"
        tvChargingHeadline.text = t.chargeSpeedLabel
        tvWattageVal.text = "${String.format(Locale.US, "%.1f", t.wattage)} W"
        tvCurrentVal.text = if (t.currentMa > 0) "+${t.currentMa} mA" else "${t.currentMa} mA"
        tvVoltageVal.text = "${String.format(Locale.US, "%.2f", t.voltageVolts)} V"
        tvTempVal.text = "🌡️ ${String.format(Locale.US, "%.1f", t.temperatureCelsius)}°C"
        tvChargerTypeVal.text = "• ${t.chargerType}"

        if (t.isCharging) {
            ivChargingIcon.setImageResource(R.drawable.ic_charging_bolt)
            tvChargingHeadline.setTextColor(ContextCompat.getColor(this, R.color.eco_green))
            tvWattageVal.setTextColor(ContextCompat.getColor(this, R.color.eco_green))

            val timeStr = BatteryAnalyticsHelper.formatDuration(t.timeRemainingMillis)
            tvTimeToFullVal.text = "⏱️ ~$timeStr to 100%"
            tvTimeToFullVal.visibility = View.VISIBLE
        } else {
            ivChargingIcon.setImageResource(R.drawable.ic_battery_saver)
            tvChargingHeadline.setTextColor(ContextCompat.getColor(this, R.color.subtext_gray))
            tvWattageVal.setTextColor(ContextCompat.getColor(this, R.color.white))

            val estHours = if (prefsManager.isExtremeModeEnabled) {
                ((t.percentage / 100.0) * 120).toInt()
            } else {
                ((t.percentage / 100.0) * 48).toInt()
            }
            tvTimeToFullVal.text = "⏱️ ~${estHours}h runtime"
            tvTimeToFullVal.visibility = View.VISIBLE
        }
    }

    private fun updatePowerSaveStateUi() {
        val isExtreme = prefsManager.isExtremeModeEnabled
        val isSuper = prefsManager.isPowerSavingEnabled

        if (isExtreme) {
            tvPowerState.text = "1% EXTREME: ON"
            tvPowerState.setTextColor(ContextCompat.getColor(this, R.color.power_orange))
            btnExitMode.visibility = View.VISIBLE

            sectionAppsHeader.visibility = View.GONE
            rvAllowedApps.visibility = View.GONE
            cardEmergencySms.visibility = View.VISIBLE

            tvExtremeTitle.text = "🔴 1% Survivor Mode Active"
            tvExtremeTitle.setTextColor(ContextCompat.getColor(this, R.color.power_red))
            tvExtremeSub.text = "Phone & SMS only • Monochrome • Ringtone works"
            btnToggleExtreme.text = "Back to 10%"
            btnToggleExtreme.setBackgroundResource(R.drawable.bg_button_dark)
            btnToggleExtreme.setTextColor(ContextCompat.getColor(this, R.color.white))

            btnTogglePower.text = "Exit Power Mode"
            btnTogglePower.setBackgroundResource(R.drawable.bg_button_dark)
            btnTogglePower.setTextColor(ContextCompat.getColor(this, R.color.white))

        } else if (isSuper) {
            tvPowerState.text = "10% Super: ON"
            tvPowerState.setTextColor(ContextCompat.getColor(this, R.color.eco_green))
            btnExitMode.visibility = View.VISIBLE

            sectionAppsHeader.visibility = View.VISIBLE
            rvAllowedApps.visibility = View.VISIBLE
            cardEmergencySms.visibility = View.GONE

            tvExtremeTitle.text = getString(R.string.extreme_mode_banner_title)
            tvExtremeTitle.setTextColor(ContextCompat.getColor(this, R.color.power_orange))
            tvExtremeSub.text = getString(R.string.extreme_mode_banner_sub)
            btnToggleExtreme.text = "Go Extreme"
            btnToggleExtreme.setBackgroundResource(R.drawable.bg_button_eco)
            btnToggleExtreme.setTextColor(ContextCompat.getColor(this, R.color.black))

            btnTogglePower.text = "Disable Power Save"
            btnTogglePower.setBackgroundResource(R.drawable.bg_button_dark)
            btnTogglePower.setTextColor(ContextCompat.getColor(this, R.color.white))

        } else {
            tvPowerState.text = "Power Save: OFF"
            tvPowerState.setTextColor(ContextCompat.getColor(this, R.color.subtext_gray))
            btnExitMode.visibility = View.GONE

            sectionAppsHeader.visibility = View.VISIBLE
            rvAllowedApps.visibility = View.VISIBLE
            cardEmergencySms.visibility = View.GONE

            tvExtremeTitle.text = getString(R.string.extreme_mode_banner_title)
            tvExtremeTitle.setTextColor(ContextCompat.getColor(this, R.color.subtext_gray))
            tvExtremeSub.text = getString(R.string.extreme_mode_banner_sub)
            btnToggleExtreme.text = "Go Extreme"
            btnToggleExtreme.setBackgroundResource(R.drawable.bg_button_dark)
            btnToggleExtreme.setTextColor(ContextCompat.getColor(this, R.color.white))

            btnTogglePower.text = "Enable 10% Power"
            btnTogglePower.setBackgroundResource(R.drawable.bg_button_eco)
            btnTogglePower.setTextColor(ContextCompat.getColor(this, R.color.black))
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

    private fun applyMonochromeGrayscale(enable: Boolean) {
        if (enable) {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            val filter = ColorMatrixColorFilter(matrix)
            val paint = Paint()
            paint.colorFilter = filter
            rootLayout.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
        } else {
            rootLayout.setLayerType(View.LAYER_TYPE_NONE, null)
        }
    }

    private fun showExtremeModeConfirmationDialog() {
        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(getString(R.string.dialog_extreme_title))
            .setMessage(getString(R.string.dialog_extreme_message))
            .setPositiveButton(getString(R.string.dialog_extreme_confirm)) { _, _ ->
                prefsManager.isExtremeModeEnabled = true
                prefsManager.isPowerSavingEnabled = true
                PowerManagerHelper.applyExtremeSurvivorProfile(this)
                applyMonochromeGrayscale(true)
                updatePowerSaveStateUi()
                Toast.makeText(this, "🔥 1% Extreme Blackout Mode Active", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(getString(R.string.dialog_exit_title))
            .setMessage(getString(R.string.dialog_exit_message))
            .setPositiveButton("Exit Mode") { _, _ ->
                prefsManager.isExtremeModeEnabled = false
                prefsManager.isPowerSavingEnabled = false
                PowerManagerHelper.restoreNormalSettings(this)
                applyMonochromeGrayscale(false)
                updatePowerSaveStateUi()
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
            tvNotificationStatus.text = "✓ Gatekeeper active (Only whitelisted apps notify)"
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
            tvWriteSettingsStatus.text = "✓ Display auto-dimming active"
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
