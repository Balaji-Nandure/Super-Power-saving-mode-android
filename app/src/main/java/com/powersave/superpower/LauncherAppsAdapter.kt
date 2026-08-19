package com.powersave.superpower

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class LauncherAppsAdapter(
    private val context: Context,
    private val appList: List<AppModel>,
    private val onAddMoreClicked: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_APP = 1
        private const val TYPE_ADD_SLOT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < appList.size) TYPE_APP else TYPE_ADD_SLOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_launcher_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val appHolder = holder as AppViewHolder
        if (position < appList.size) {
            val app = appList[position]
            appHolder.tvName.text = app.appName
            appHolder.ivIcon.setImageDrawable(app.icon)
            appHolder.tvStatus.text = "Allowed"
            appHolder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.eco_green))

            appHolder.itemView.setOnClickListener {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                } else {
                    Toast.makeText(context, "Cannot open ${app.appName}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Add slot for configuring whitelist (up to 25)
            appHolder.tvName.text = "+ Add / Manage Apps"
            appHolder.ivIcon.setImageResource(R.drawable.ic_apps)
            appHolder.tvStatus.text = "${appList.size}/${PreferencesManager.MAX_ALLOWED_APPS} Selected"
            appHolder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.subtext_gray))

            appHolder.itemView.setOnClickListener {
                onAddMoreClicked()
            }
        }
    }

    override fun getItemCount(): Int {
        return if (appList.size < PreferencesManager.MAX_ALLOWED_APPS) {
            appList.size + 1
        } else {
            PreferencesManager.MAX_ALLOWED_APPS
        }
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivLauncherAppIcon)
        val tvName: TextView = itemView.findViewById(R.id.tvLauncherAppName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvAppStatus)
    }
}
