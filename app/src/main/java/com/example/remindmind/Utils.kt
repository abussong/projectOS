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

/**
 * Утилитарный класс с вспомогательными функциями.
 *
 * Содержит методы для генерации ID, форматирования дат,
 * проверки времени и создания PendingIntent для уведомлений.
 *
 * @author Грехов М.В., Яньшина А.Ю.
 * @since 1.0.0
 * @version 2.0.0
 */
class Utils {
    companion object {

        /**
         * Генерирует уникальный ID для напоминания или подзадачи.
         *
         * Использует текущее время в миллисекундах, взятое по модулю 1000000.
         *
         * @return Уникальный целочисленный ID
         *
         * @author Грехов М.В., Яньшина А.Ю.
         * @since 1.0.0
         */
        fun getID(): Int {
            return (System.currentTimeMillis() % 1000000).toInt()
        }

        /**
         * Добавляет ведущий ноль к числу, если оно меньше 10.
         *
         * Используется для форматирования даты и времени (день, месяц, час, минута).
         *
         * @param count Число для форматирования
         * @return Строка с ведущим нулём (например, "05" или "12")
         *
         * @author Грехов М.В., Яньшина А.Ю.
         * @since 1.0.0
         */
        fun addZero(count: Int): String {
            return if (count < 10) "0$count" else count.toString()
        }

        /**
         * Возвращает текущую дату в формате dd.MM.yyyy.
         *
         * @return Текущая дата в формате "день.месяц.год"
         *
         * @author Грехов М.В., Яньшина А.Ю.
         * @since 1.0.0
         */
        fun getCurrentDate(): String {
            val currentDate = Date()
            val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            return formatter.format(currentDate)
        }

        /**
         * Проверяет, наступило ли уже время напоминания.
         *
         * Сравнивает заданные дату и время с текущим моментом.
         *
         * @param date Дата в формате dd.MM.yyyy
         * @param time Время в формате HH:mm
         * @return true, если время уже прошло, false в противном случае
         *
         * @author Грехов М.В., Яньшина А.Ю.
         * @since 1.0.0
         */
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

        /**
         * Создаёт PendingIntent для BroadcastReceiver уведомления (базовая версия).
         *
         * @param context Контекст приложения
         * @param id ID уведомления
         * @param text Текст уведомления
         * @return PendingIntent для отправки уведомления
         *
         * @author Грехов М.В., Яньшина А.Ю.
         * @since 1.0.0
         */
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

        /**
         * Создаёт PendingIntent для BroadcastReceiver уведомления с настройками.
         *
         * Добавляет в Intent дополнительные параметры: звук и вибрацию.
         *
         * @param context Контекст приложения
         * @param id ID уведомления
         * @param text Текст уведомления
         * @param settings Настройки уведомления (звук, вибрация)
         * @return PendingIntent с расширенными настройками
         *
         * @author Яньшина А.Ю.
         * @since 2.0.0
         */
        fun getPendingIntentWithSettings(context: Context, id: Int, text: String, settings: NotificationSettings): PendingIntent {
            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                putExtra("text", text)
                putExtra("id", id)
                putExtra("sound_uri", settings.soundUri.toString())
                putExtra("vibration_enabled", settings.isVibrationEnabled)
            }
            return PendingIntent.getBroadcast(
                context, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        /**
         * Преобразует приоритет в числовое значение для сортировки.
         *
         * @param priority Приоритет задачи
         * @return Числовое значение: 3 для HIGH, 2 для MEDIUM, 1 для LOW
         *
         * @author Грехов М.В.
         * @since 1.1.0
         */
        fun getPriorityValue(priority: Priority): Int {
            return when (priority) {
                Priority.HIGH -> 3
                Priority.MEDIUM -> 2
                Priority.LOW -> 1
            }
        }
    }
}