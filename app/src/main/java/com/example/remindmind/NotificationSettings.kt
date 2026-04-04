package com.example.remindmind

import android.net.Uri
import android.provider.Settings
import java.io.Serializable

enum class RepeatType {
    ONCE,
    DAILY,
    WEEKLY
}

enum class WeekDay(val value: Int) {
    MONDAY(2),
    TUESDAY(3),
    WEDNESDAY(4),
    THURSDAY(5),
    FRIDAY(6),
    SATURDAY(7),
    SUNDAY(1)
}

data class NotificationSettings(
    val repeatType: RepeatType = RepeatType.ONCE,
    val selectedDays: Set<WeekDay> = emptySet(),
    val soundUri: Uri = Settings.System.DEFAULT_NOTIFICATION_URI,
    val isVibrationEnabled: Boolean = true,
    val repeatCount: Int = 0,
    val repeatIntervalMinutes: Int = 5
) : Serializable {
    // Добавляем метод для сериализации URI в строку
    fun getSoundUriString(): String = soundUri.toString()

    // Добавляем companion object для десериализации
    companion object {
        fun fromSoundUriString(uriString: String): Uri {
            return if (uriString.isNotEmpty()) {
                try {
                    Uri.parse(uriString)
                } catch (e: Exception) {
                    Settings.System.DEFAULT_NOTIFICATION_URI
                }
            } else {
                Settings.System.DEFAULT_NOTIFICATION_URI
            }
        }
    }
}