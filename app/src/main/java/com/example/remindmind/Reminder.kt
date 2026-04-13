package com.example.remindmind

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf

/**
 * Перечисление приоритетов задач.
 *
 * @property HIGH Высокий приоритет (красный индикатор)
 * @property MEDIUM Средний приоритет (жёлтый индикатор)
 * @property LOW Низкий приоритет (зелёный индикатор)
 *
 * @author Грехов М.В.
 * @since 1.1.0
 */
enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}

/**
 * Класс подзадачи.
 *
 * Подзадачи привязаны к родительскому напоминанию и имеют собственные
 * дату, время, приоритет и настройки уведомлений.
 *
 * @property id Уникальный идентификатор подзадачи
 * @property text Текст подзадачи
 * @property date Дата выполнения (формат dd.MM.yyyy)
 * @property time Время выполнения (формат HH:mm)
 * @property isCompleted Статус выполнения (по умолчанию false)
 * @property priority Приоритет подзадачи (по умолчанию MEDIUM)
 * @property notificationSettings Настройки уведомления для подзадачи (добавлены в версии 2.0.0)
 *
 * @author Яньшина А.Ю.
 * @since 1.2.0
 * @version 2.0.0
 */
data class SubTask(
    val id: Int,
    var text: String,
    val date: String,
    val time: String,
    var isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val notificationSettings: NotificationSettings = NotificationSettings()
)

/**
 * Класс напоминания (основной задачи).
 *
 * Содержит основную информацию о задаче, список подзадач и настройки уведомлений.
 *
 * @property id Уникальный идентификатор напоминания
 * @property text Текст напоминания
 * @property date Дата выполнения (формат dd.MM.yyyy)
 * @property time Время выполнения (формат HH:mm)
 * @property isCompleted Статус выполнения (по умолчанию false)
 * @property priority Приоритет задачи (по умолчанию MEDIUM)
 * @property subTasks Список подзадач (добавлен в версии 1.2.0)
 * @property notificationSettings Настройки уведомления (добавлены в версии 2.0.0)
 *
 * @author Грехов М.В., Яньшина А.Ю.
 * @since 1.0.0
 * @version 2.0.0
 */
data class Reminder(
    val id: Int,
    var text: String,
    val date: String,
    val time: String,
    var isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val subTasks: SnapshotStateList<SubTask> = mutableStateListOf(),
    val notificationSettings: NotificationSettings = NotificationSettings()
)