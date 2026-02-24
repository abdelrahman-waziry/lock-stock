// android/app/src/main/kotlin/com/example/lockwidgetapp/LockDeviceAdminReceiver.kt
package com.example.lockwidgetapp

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class LockDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Lock Widget: Device Admin enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Lock Widget: Device Admin disabled", Toast.LENGTH_SHORT).show()
    }
}
