package com.example.remindmind

import android.net.Uri
import android.provider.Settings
import java.io.Serializable

/**
 * Перечисление типов повторения уведомлений.
 *
 * @property ONCE Однократное уведомление
 * @property DAILY Ежедневное уведомление
 * @property WEEKLY Еженедельное уведомление (по выбранным дням)
 *
 * @author Яньшина А.Ю.
 * @since 2.0.0
 */
enum class RepeatType {
    ONCE,
    DAILY,
    WEEKLY
}

/**
 * Перечисление дней недели для еженедельного повтора.
 *
 * Значения соответствуют константам Calendar.DAY_OF_WEEK.
 *
 * @property value Числовое значение дня недели (Calendar.DAY_OF_WEEK)
 *
 * @author Яньшина А.Ю.
 * @since 2.0.0
 */
enum class WeekDay(val value: Int) {
    MONDAY(2),
    TUESDAY(3),
    WEDNESDAY(4),
    THURSDAY(5),
    FRIDAY(6),
    SATURDAY(7),
    SUNDAY(1)
}

/**
 * Настройки уведомления для задачи или подзадачи.
 *
 * Позволяет настроить периодичность, звук, вибрацию и повторы.
 *
 * @property repeatType Тип повторения (один раз, ежедневно, еженедельно)
 * @property selectedDays Выбранные дни для еженедельного повтора
 * @property soundUri URI звука уведомления
 * @property isVibrationEnabled Включена ли вибрация
 * @property repeatCount Количество дополнительных повторов (0-10)
 * @property repeatIntervalMinutes Интервал между повторами в минутах (1-60)
 *
 * @author Яньшина А.Ю.
 * @since 2.0.0
 */
data class NotificationSettings(
    val repeatType: RepeatType = RepeatType.ONCE,
    val selectedDays: Set<WeekDay> = emptySet(),
    val soundUri: Uri = Settings.System.DEFAULT_NOTIFICATION_URI,
    val isVibrationEnabled: Boolean = true,
    val repeatCount: Int = 0,
    val repeatIntervalMinutes: Int = 5
) : Serializable {

    /**
     * Преобразует URI звука в строку для сохранения в базу данных.
     *
     * @return Строковое представление URI
     */
    fun getSoundUriString(): String = soundUri.toString()

    companion object {
        /**
         * Восстанавливает URI звука из строки.
         *
         * @param uriString Строковое представление URI
         * @return Объект Uri или стандартный звук при ошибке
         */
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