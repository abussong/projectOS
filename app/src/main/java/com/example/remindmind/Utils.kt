
package com.example.remindmind

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Utils {
    companion object {
        private var lastId = 0

        fun getID(): Int {
            return (System.currentTimeMillis() % 1000000).toInt()
        }

        fun addZero(count: Int): String {
            return if (count < 10) "0$count" else count.toString()
        }

        fun getCurrentDate(): String {
            val currentDate = Date()
            val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            return formatter.format(currentDate)
        }

        fun isReminderInPast(date: String, time: String): Boolean {
            try {
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                val reminderDateTime = LocalDateTime.parse("$date $time", formatter)
                val now = LocalDateTime.now(ZoneId.systemDefault())
                return reminderDateTime.isBefore(now)
            } catch (e: Exception) {
                return false
            }
        }

        fun getPendingIntent(context: Context, id: Int, text: String): PendingIntent {
            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                putExtra("text", text)
                putExtra("id", id)
            }
            return PendingIntent.getBroadcast(
                context, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun getPriorityValue(priority: Priority): Int {
            return when (priority) {
                Priority.HIGH -> 3
                Priority.MEDIUM -> 2
                Priority.LOW -> 1
            }
        }
    }
}