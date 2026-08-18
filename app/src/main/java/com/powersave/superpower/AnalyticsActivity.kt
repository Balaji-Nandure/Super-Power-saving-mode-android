package com.powersave.superpower

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var batteryGraphView: BatteryGraphView
    private lateinit var tvScreenOnDrain: TextView
    private lateinit var tvScreenOffDrain: TextView
    private lateinit var btnToggleTurboCharge: Button
    private lateinit var tvTurboDesc: TextView
    private lateinit var tvDiagVoltage: TextView
    private lateinit var tvDiagTemp: TextView
    private lateinit var tvDiagHealth: TextView
    private lateinit var tvDiagTech: TextView
    private lateinit var btnClose: Button

    private var isTurboActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        initViews()
        loadAnalyticsData()
    }

    private fun initViews() {
        batteryGraphView = findViewById(R.id.batteryGraphView)
        tvScreenOnDrain = findViewById(R.id.tvScreenOnDrain)
        tvScreenOffDrain = findViewById(R.id.tvScreenOffDrain)
        btnToggleTurboCharge = findViewById(R.id.btnToggleTurboCharge)
        tvTurboDesc = findViewById(R.id.tvTurboDesc)
        tvDiagVoltage = findViewById(R.id.tvDiagVoltage)
        tvDiagTemp = findViewById(R.id.tvDiagTemp)
        tvDiagHealth = findViewById(R.id.tvDiagHealth)
        tvDiagTech = findViewById(R.id.tvDiagTech)
        btnClose = findViewById(R.id.btnCloseAnalytics)

        btnClose.setOnClickListener {
            finish()
        }

        btnToggleTurboCharge.setOnClickListener {
            isTurboActive = !isTurboActive
            if (isTurboActive) {
                TurboChargeController.activateTurboCharging(this)
                btnToggleTurboCharge.text = "Active"
                btnToggleTurboCharge.setBackgroundResource(R.drawable.bg_button_dark)
                tvTurboDesc.text = "⚡ Turbo Charging is ACTIVE. Background heat & drain killed for maximum charging speed."
                Toast.makeText(this, "⚡ Turbo Fast Charging ACTIVATED", Toast.LENGTH_SHORT).show()
            } else {
                TurboChargeController.deactivateTurboCharging(this)
                btnToggleTurboCharge.text = "Boost Charge"
                btnToggleTurboCharge.setBackgroundResource(R.drawable.bg_button_eco)
                tvTurboDesc.text = "Kills background tasks & heat to accelerate charging by up to +40%"
                Toast.makeText(this, "Turbo Fast Charging DEACTIVATED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadAnalyticsData() {
        val historyManager = BatteryHistoryManager(this)
        val historyPoints = historyManager.getHistoryData()
        batteryGraphView.setData(historyPoints)

        val (screenOnRate, screenOffRate) = historyManager.computeDrainStats()
        tvScreenOnDrain.text = "${String.format("%.1f", screenOnRate)}% / hr"
        tvScreenOffDrain.text = "${String.format("%.2f", screenOffRate)}% / hr"

        val t = BatteryAnalyticsHelper.getRealTimeTelemetry(this)
        tvDiagVoltage.text = "• Voltage: ${String.format("%.2f", t.voltageVolts)} V (${String.format("%.1f", t.wattage)}W)"
        tvDiagTemp.text = "• Temperature: ${String.format("%.1f", t.temperatureCelsius)}°C (${if (t.temperatureCelsius < 36.0) "Optimal - No Thermal Throttling" else "Warm"})"
        tvDiagHealth.text = "• Battery Health: ${t.health}"
        tvDiagTech.text = "• Chemistry: ${t.technology} (High Efficiency)"
    }
}
