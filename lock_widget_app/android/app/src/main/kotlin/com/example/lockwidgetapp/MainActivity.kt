// android/app/src/main/kotlin/com/example/lockwidgetapp/MainActivity.kt
package com.example.lockwidgetapp

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val channelName = "com.example.lockwidgetapp/lock"
    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, LockDeviceAdminReceiver::class.java)
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
            .setMethodCallHandler { call, result ->
                when (call.method) {

                    // Check whether device-admin is already granted
                    "isAdminActive" -> {
                        result.success(dpm.isAdminActive(adminComponent))
                    }

                    // Open the system Device Admin activation screen
                    "requestAdmin" -> {
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                            putExtra(
                                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                "Required to lock the screen from the home-screen widget."
                            )
                        }
                        startActivity(intent)
                        result.success(null)
                    }

                    // Lock the screen immediately
                    "lockScreen" -> {
                        if (dpm.isAdminActive(adminComponent)) {
                            dpm.lockNow()
                            result.success(null)
                        } else {
                            result.error(
                                "NOT_ADMIN",
                                "Device Admin is not active. Grant it first.",
                                null
                            )
                        }
                    }

                    else -> result.notImplemented()
                }
            }
    }
}
