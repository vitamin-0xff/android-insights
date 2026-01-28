package com.example.phone_checker

import android.app.Application
import android.util.Log
import com.example.phone_checker.data.monitors.MonitorManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HiltApplication : Application() {

    @Inject
    lateinit var monitorManager: MonitorManager

    companion object {
        private const val TAG = "HiltApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application created, initializing monitors")

        // Start application-wide monitors
        monitorManager.startAllMonitors()
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "Application terminating, cleaning up monitors")
        monitorManager.cleanupAllMonitors()
    }
}