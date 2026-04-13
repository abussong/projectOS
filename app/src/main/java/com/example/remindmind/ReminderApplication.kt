package com.example.remindmind

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jakewharton.threetenabp.AndroidThreeTen

/**
 * Класс приложения RemindMind.
 *
 * Инициализирует библиотеку ThreeTenABP для работы с датами и временем,
 * а также создаёт канал уведомлений для Android 8+ с высоким приоритетом.
 *
 * @author Грехов М.В., Яньшина А.Ю.
 * @since 1.0.0
 * @version 2.0.0
 */
class ReminderApplication : Application() {
    companion object {
        /** Название канала уведомлений */
        const val channelName = "Reminders"

        /** Описание канала уведомлений */
        const val channelDescription = "Channel for reminders"

        /** ID канала уведомлений */
        const val channelId = "reminders"
    }

    /**
     * Вызывается при создании приложения.
     *
     * Инициализирует ThreeTenABP и создаёт канал уведомлений с высоким приоритетом
     * для поддержки всплывающих уведомлений (heads-up notifications).
     *
     * @author Грехов М.В., Яньшина А.Ю.
     * @since 1.0.0
     */
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Удаляем старый канал, если он существует (для обновления настроек)
            try {
                notificationManager.deleteNotificationChannel(channelId)
            } catch (e: Exception) {
                android.util.Log.e("ReminderApplication", "Error deleting channel: ${e.message}")
            }

            // Создаём канал с IMPORTANCE_HIGH для всплывающих уведомлений
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