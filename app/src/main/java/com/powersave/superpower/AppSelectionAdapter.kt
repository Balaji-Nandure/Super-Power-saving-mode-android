package com.powersave.superpower

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppSelectionAdapter(
    private var allApps: List<AppModel>,
    private val selectedPackages: MutableSet<String>,
    private val onSelectionChanged: (selectedCount: Int) -> Unit,
    private val onMaxLimitReached: () -> Unit
) : RecyclerView.Adapter<AppSelectionAdapter.AppViewHolder>() {

    private var displayedApps: List<AppModel> = allApps.toList()

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivAppIcon)
        val tvName: TextView = itemView.findViewById(R.id.tvAppName)
        val tvPackage: TextView = itemView.findViewById(R.id.tvPackageName)
        val cbSelected: CheckBox = itemView.findViewById(R.id.cbSelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = displayedApps[position]
        holder.tvName.text = app.appName
        holder.tvPackage.text = app.packageName
        holder.ivIcon.setImageDrawable(app.icon)

        val isSelected = selectedPackages.contains(app.packageName)
        holder.cbSelected.isChecked = isSelected

        holder.itemView.setOnClickListener {
            if (selectedPackages.contains(app.packageName)) {
                selectedPackages.remove(app.packageName)
                holder.cbSelected.isChecked = false
                onSelectionChanged(selectedPackages.size)
            } else {
                if (selectedPackages.size >= PreferencesManager.MAX_ALLOWED_APPS) {
                    onMaxLimitReached()
                } else {
                    selectedPackages.add(app.packageName)
                    holder.cbSelected.isChecked = true
                    onSelectionChanged(selectedPackages.size)
                }
            }
        }
    }

    override fun getItemCount(): Int = displayedApps.size

    fun updateList(newList: List<AppModel>) {
        allApps = newList
        displayedApps = newList.toList()
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        displayedApps = if (query.isBlank()) {
            allApps
        } else {
            allApps.filter {
                it.appName.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}
