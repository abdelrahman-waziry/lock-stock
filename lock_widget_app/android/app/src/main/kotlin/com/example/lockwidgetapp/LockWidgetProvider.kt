// android/app/src/main/kotlin/com/example/lockwidgetapp/LockWidgetProvider.kt
package com.example.lockwidgetapp

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class LockWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_LOCK = "com.example.lockwidgetapp.LOCK_ACTION"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_LOCK) {
            // Lock directly from the widget without opening the app
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = ComponentName(context, LockDeviceAdminReceiver::class.java)
            if (dpm.isAdminActive(admin)) {
                dpm.lockNow()
            } else {
                // If admin not granted, open the app so the user can grant it
                val launchIntent = context.packageManager
                    .getLaunchIntentForPackage(context.packageName)
                    ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                launchIntent?.let { context.startActivity(it) }
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.lock_widget_layout)

        // Pending intent that sends the LOCK_ACTION broadcast back to this receiver
        val lockIntent = Intent(context, LockWidgetProvider::class.java).apply {
            action = ACTION_LOCK
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            lockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_lock, pendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }
}
