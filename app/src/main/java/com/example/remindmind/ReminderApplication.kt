package com.example.remindmind

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jakewharton.threetenabp.AndroidThreeTen

class ReminderApplication : Application() {
    companion object {
        const val channelName = "Reminders"
        const val channelDescription = "Channel for reminders"
        const val channelId = "reminders"
    }

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Удаляем старый канал, если он существует
            try {
                notificationManager.deleteNotificationChannel(channelId)
            } catch (e: Exception) {
                // Канал может не существовать
                android.util.Log.e("ReminderApplication", "Error deleting channel: ${e.message}")
            }

            // Создаем канал с IMPORTANCE_HIGH
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDescription
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)

            android.util.Log.d("ReminderApplication", "Notification channel created with IMPORTANCE_HIGH")
        }
    }
}