package com.powersave.superpower

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class AppSelectionActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager
    private lateinit var adapter: AppSelectionAdapter
    private lateinit var tvSelectionCount: TextView
    private lateinit var btnSave: Button
    private lateinit var etSearch: EditText
    private lateinit var rvApps: RecyclerView

    private val selectedPackages = mutableSetOf<String>()
    private val allAppsList = mutableListOf<AppModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_selection)

        prefsManager = PreferencesManager(this)
        selectedPackages.addAll(prefsManager.getWhitelistedPackages())

        initViews()
        loadInstalledApps()
        updateCounter(selectedPackages.size)
    }

    private fun initViews() {
        tvSelectionCount = findViewById(R.id.tvSelectionCount)
        btnSave = findViewById(R.id.btnSaveSelection)
        etSearch = findViewById(R.id.etSearch)
        rvApps = findViewById(R.id.rvAppsList)

        rvApps.layoutManager = LinearLayoutManager(this)

        adapter = AppSelectionAdapter(
            allApps = allAppsList,
            selectedPackages = selectedPackages,
            onSelectionChanged = { count ->
                updateCounter(count)
            },
            onMaxLimitReached = {
                Toast.makeText(this, getString(R.string.toast_max_reached), Toast.LENGTH_SHORT).show()
            }
        )
        rvApps.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSave.setOnClickListener {
            prefsManager.saveWhitelistedPackages(selectedPackages)
            Toast.makeText(this, getString(R.string.toast_selection_saved), Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun updateCounter(count: Int) {
        tvSelectionCount.text = "$count/${PreferencesManager.MAX_ALLOWED_APPS} Selected"
        btnSave.text = "Save Whitelist ($count/${PreferencesManager.MAX_ALLOWED_APPS})"
    }

    private fun loadInstalledApps() {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

        allAppsList.clear()
        for (resolveInfo in resolveInfos) {
            val packageName = resolveInfo.activityInfo.packageName
            // Skip our own app from the list
            if (packageName == this.packageName) continue

            val appName = resolveInfo.loadLabel(pm).toString()
            val icon = resolveInfo.loadIcon(pm)
            val isSelected = selectedPackages.contains(packageName)

            allAppsList.add(AppModel(appName, packageName, icon, isSelected))
        }

        // Sort alphabetically
        allAppsList.sortBy { it.appName.lowercase() }
        adapter.updateList(allAppsList)
    }
}
