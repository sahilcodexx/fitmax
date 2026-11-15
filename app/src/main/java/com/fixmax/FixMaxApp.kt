package com.fixmax

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class FixMaxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "step_tracking_channel",
                "Step Tracking Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Continuous motion and step tracking service"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
