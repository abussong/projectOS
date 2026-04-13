package com.example.remindmind

import android.app.AlarmManager
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
/**
 * ViewModel для управления напоминаниями и подзадачами.
 *
 * Содержит всю бизнес-логику приложения: создание, редактирование, удаление задач,
 * управление уведомлениями, сортировку и автоматическую очистку.
 *
 * @author Грехов М.В. (приоритеты, темы, проверки времени, редактирование текста)
 * @author Яньшина А.Ю. (подзадачи, кастомизация уведомлений, автоматическая очистка)
 * @since 1.0.0
 * @version 2.2.0
 */
class RemindersViewModel : ViewModel() {
    // ==================== Базовые поля ====================

    /** Помощник для работы с базой данных */
    lateinit var dbHelper: DatabaseHelper

    /** Менеджер будильников для планирования уведомлений */
    lateinit var alarmManager: AlarmManager

    /** Текст новой задачи */
    var text by mutableStateOf("")

    /** Дата новой задачи (формат dd.MM.yyyy) */
    var date by mutableStateOf("")

    /** Время новой задачи (формат HH:mm) */
    var time by mutableStateOf("")

    /** Приоритет новой задачи */
    var selectedPriority by mutableStateOf(Priority.MEDIUM)

    /** Временные настройки уведомления для новой задачи (добавлены в версии 2.0.0) */
    var tempNotificationSettings by mutableStateOf(NotificationSettings())

    // ==================== Поля для подзадач ====================

    /** Флаг отображения диалога создания подзадачи (добавлено в версии 1.2.0) */
    var showSubTaskDialog by mutableStateOf(false)

    /** Текущее напоминание, для которого создается подзадача */
    var currentReminderForSubTask: Reminder? by mutableStateOf(null)

    /** Текст новой подзадачи */
    var subTaskText by mutableStateOf("")

    /** Дата новой подзадачи */
    var subTaskDate by mutableStateOf("")

    /** Время новой подзадачи */
    var subTaskTime by mutableStateOf("")

    /** Приоритет новой подзадачи (добавлен в версии 1.1.0, для подзадач с 1.2.0) */
    var subTaskPriority by mutableStateOf(Priority.MEDIUM)

    /** Настройки уведомления для подзадачи (добавлены в версии 2.0.0) */
    var subTaskNotificationSettings by mutableStateOf(NotificationSettings())

    // ==================== Список напоминаний ====================

    /** Список всех напоминаний (автоматически обновляется в UI) */
    val reminders = mutableStateListOf<Reminder>()

    // ==================== Редактирование текста (добавлено в версии 2.1.0) ====================

    /**
     * Редактирует текст существующего напоминания.
     *
     * Обновляет текст в объекте, базе данных и перепланирует уведомление
     * с новым текстом, если задача не выполнена и не просрочена.
     *
     * @param reminder Напоминание для редактирования
     * @param newText Новый текст напоминания
     * @param context Контекст приложения
     *
     * @author Грехов М.В.
     * @since 2.1.0
     */
    fun editReminderText(reminder: Reminder, newText: String, context: Context) {
        if (newText.isBlank()) return
        cancelNotification(context, reminder.id)
        reminder.text = newText
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_TEXT, newText)
        }
        dbHelper.writableDatabase?.update(
            DatabaseHelper.TABLE_REMINDERS,
            values,
            "${DatabaseHelper.COLUMN_ID}=?",
            arrayOf(reminder.id.toString())
        )

        // Если задача не выполнена и не просрочена - планируем новое уведомление с новым текстом
        if (!reminder.isCompleted && !Utils.isReminderInPast(reminder.date, reminder.time)) {
            scheduleNotificationWithSettings(context, reminder)
        }
        val index = reminders.indexOf(reminder)
        if (index != -1) {
            reminders[index] = reminder.copy(text = newText, isCompleted = reminder.isCompleted)
        }

        sortReminders()
        Toast.makeText(context, R.string.toast_task_updated, Toast.LENGTH_LONG).show()
    }
    /**
     * Редактирует текст существующей подзадачи.
     *
     * @param reminder Родительское напоминание
     * @param subTask Подзадача для редактирования
     * @param newText Новый текст подзадачи
     * @param context Контекст приложения
     *
     * @author Грехов М.В.
     * @since 2.1.0
     */
    fun editSubTaskText(reminder: Reminder, subTask: SubTask, newText: String, context: Context) {
        if (newText.isBlank()) return
        cancelNotification(context, subTask.id)
        subTask.text = newText

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SUBTASK_TEXT, newText)
        }
        dbHelper.writableDatabase?.update(
            DatabaseHelper.TABLE_SUBTASKS,
            values,
            "${DatabaseHelper.COLUMN_SUBTASK_ID}=?",
            arrayOf(subTask.id.toString())
        )


        if (!subTask.isCompleted && !Utils.isReminderInPast(subTask.date, subTask.time)) {
            scheduleNotificationWithSettings(context, subTask)
        }


        val index = reminder.subTasks.indexOf(subTask)
        if (index != -1) {
            reminder.subTasks[index] = subTask.copy(text = newText, isCompleted = subTask.isCompleted)
        }

        sortReminders()
        Toast.makeText(context, R.string.toast_subtask_updated, Toast.LENGTH_LONG).show()
    }
    /**
     * Добавляет новое напоминание.
     *
     * Проверяет корректность даты и времени (нельзя установить время в прошлом),
     * сохраняет задачу в базу данных и планирует уведомление.
     *
     * @param context Контекст приложения
     *
     * @author Грехов М.В., Яньшина А.Ю.
     * @since 1.0.0
     */
    fun addReminder(context: Context) {
        // Проверяем, заполнена ли дата и время
        if (date.isEmpty() && time.isEmpty()) {
            Toast.makeText(context, R.string.toast_datetime_error, Toast.LENGTH_LONG).show()
            return
        } else if (text.isEmpty()) {
            Toast.makeText(context, R.string.toast_text_error, Toast.LENGTH_LONG).show()
            return
        }

        val finalDate = if (date.isEmpty()) Utils.getCurrentDate() else date
        val finalTime = if (time.isEmpty()) "12:00" else time
        // Проверка: нельзя установить время в прошлом (добавлено в версии 1.2.1)
        if (isTodayForMain(finalDate)) {
            val currentCalendar = Calendar.getInstance()
            val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = currentCalendar.get(Calendar.MINUTE)

            val timeParts = finalTime.split(":")
            val selectedHour = timeParts[0].toInt()
            val selectedMinute = timeParts[1].toInt()

            if (selectedHour < currentHour || (selectedHour == currentHour && selectedMinute < currentMinute)) {
                Toast.makeText(context, R.string.time_cannot_be_past, Toast.LENGTH_LONG).show()
                return
            }
        }

        val reminder = Reminder(
            id = Utils.getID(),
            text = text,
            date = finalDate,
            time = finalTime,
            priority = selectedPriority,
            notificationSettings = tempNotificationSettings
        )
        reminders.add(reminder)

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_ID, reminder.id)
            put(DatabaseHelper.COLUMN_TEXT, reminder.text)
            put(DatabaseHelper.COLUMN_DATE, reminder.date)
            put(DatabaseHelper.COLUMN_TIME, reminder.time)
            put(DatabaseHelper.COLUMN_IS_COMPLETED, if (reminder.isCompleted) 1 else 0)
            put(DatabaseHelper.COLUMN_PRIORITY, reminder.priority.name)

            put(DatabaseHelper.COLUMN_REPEAT_TYPE, reminder.notificationSettings.repeatType.name)
            put(DatabaseHelper.COLUMN_SELECTED_DAYS, reminder.notificationSettings.selectedDays.joinToString(",") { it.name })
            put(DatabaseHelper.COLUMN_SOUND_URI, reminder.notificationSettings.soundUri.toString())
            put(DatabaseHelper.COLUMN_VIBRATION_ENABLED, if (reminder.notificationSettings.isVibrationEnabled) 1 else 0)
            put(DatabaseHelper.COLUMN_REPEAT_COUNT, reminder.notificationSettings.repeatCount)
            put(DatabaseHelper.COLUMN_REPEAT_INTERVAL, reminder.notificationSettings.repeatIntervalMinutes)
        }
        dbHelper.writableDatabase?.insert(DatabaseHelper.TABLE_REMINDERS, null, values)


        text = ""
        date = ""
        time = ""
        selectedPriority = Priority.MEDIUM
        tempNotificationSettings = NotificationSettings()

        if (!reminder.isCompleted && !Utils.isReminderInPast(reminder.date, reminder.time)) {
            scheduleNotificationWithSettings(context, reminder)
        }

        sortReminders()
        Toast.makeText(context, R.string.toast_task_created, Toast.LENGTH_LONG).show()
    }

    /**
     * Добавляет новую подзадачу к существующему напоминанию.
     *
     * @param context Контекст приложения
     * @param parentReminder Родительское напоминание
     *
     * @author Яньшина А.Ю.
     * @since 1.2.0
     */
    fun addSubTask(context: Context, parentReminder: Reminder) {
        if (subTaskDate.isEmpty() && subTaskTime.isEmpty()) {
            Toast.makeText(context, R.string.toast_datetime_error, Toast.LENGTH_LONG).show()
            return
        } else if (subTaskText.isEmpty()) {
            Toast.makeText(context, R.string.toast_text_error, Toast.LENGTH_LONG).show()
            return
        }

        val finalDate = if (subTaskDate.isEmpty()) Utils.getCurrentDate() else subTaskDate
        val finalTime = if (subTaskTime.isEmpty()) "12:00" else subTaskTime

        val uniqueId = (System.currentTimeMillis() % 1000000).toInt()

        val subTask = SubTask(
            id = uniqueId,
            text = subTaskText,
            date = finalDate,
            time = finalTime,
            priority = subTaskPriority,
            notificationSettings = subTaskNotificationSettings
        )

        parentReminder.subTasks.add(subTask)

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SUBTASK_ID, subTask.id)
            put(DatabaseHelper.COLUMN_PARENT_ID, parentReminder.id)
            put(DatabaseHelper.COLUMN_SUBTASK_TEXT, subTask.text)
            put(DatabaseHelper.COLUMN_SUBTASK_DATE, subTask.date)
            put(DatabaseHelper.COLUMN_SUBTASK_TIME, subTask.time)
            put(DatabaseHelper.COLUMN_SUBTASK_IS_COMPLETED, if (subTask.isCompleted) 1 else 0)
            put(DatabaseHelper.COLUMN_SUBTASK_PRIORITY, subTask.priority.name)

            put(DatabaseHelper.COLUMN_SUBTASK_REPEAT_TYPE, subTask.notificationSettings.repeatType.name)
            put(DatabaseHelper.COLUMN_SUBTASK_SELECTED_DAYS, subTask.notificationSettings.selectedDays.joinToString(",") { it.name })
            put(DatabaseHelper.COLUMN_SUBTASK_SOUND_URI, subTask.notificationSettings.soundUri.toString())
            put(DatabaseHelper.COLUMN_SUBTASK_VIBRATION_ENABLED, if (subTask.notificationSettings.isVibrationEnabled) 1 else 0)
            put(DatabaseHelper.COLUMN_SUBTASK_REPEAT_COUNT, subTask.notificationSettings.repeatCount)
            put(DatabaseHelper.COLUMN_SUBTASK_REPEAT_INTERVAL, subTask.notificationSettings.repeatIntervalMinutes)
        }
        dbHelper.writableDatabase?.insert(DatabaseHelper.TABLE_SUBTASKS, null, values)

        if (!subTask.isCompleted && !Utils.isReminderInPast(subTask.date, subTask.time)) {
            scheduleNotificationWithSettings(context, subTask)
        }

        subTaskText = ""
        subTaskDate = ""
        subTaskTime = ""
        subTaskPriority = Priority.MEDIUM
        subTaskNotificationSettings = NotificationSettings()
        showSubTaskDialog = false

        sortReminders()
        Toast.makeText(context, R.string.toast_subtask_created, Toast.LENGTH_LONG).show()
    }
    /**
     * Переключает статус выполнения напоминания (выполнено/не выполнено).
     *
     * При выполнении задачи отменяет уведомление.
     * При снятии отметки планирует новое уведомление, если время не прошло.
     *
     * @param reminder Напоминание для переключения
     * @param context Контекст приложения
     *
     * @author Яньшина А.Ю.
     * @since 1.2.0
     */
    fun toggleReminderCompletion(reminder: Reminder, context: Context) {
        reminder.isCompleted = !reminder.isCompleted
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_IS_COMPLETED, if (reminder.isCompleted) 1 else 0)
        }
        dbHelper.writableDatabase?.update(
            DatabaseHelper.TABLE_REMINDERS,
            values,
            "${DatabaseHelper.COLUMN_ID}=?",
            arrayOf(reminder.id.toString())
        )

        if (reminder.isCompleted) {
            cancelNotification(context, reminder.id)
        } else {
            if (!Utils.isReminderInPast(reminder.date, reminder.time)) {
                scheduleNotificationWithSettings(context, reminder)
            }
        }

        sortReminders()
        val index = reminders.indexOf(reminder)
        if (index != -1) {
            reminders[index] = reminder.copy(isCompleted = reminder.isCompleted)
        }
    }
    /**
     * Переключает статус выполнения подзадачи.
     *
     * @param reminder Родительское напоминание
     * @param subTask Подзадача для переключения
     * @param context Контекст приложения
     *
     * @author Яньшина А.Ю.
     * @since 1.2.0
     */
    fun toggleSubTaskCompletion(reminder: Reminder, subTask: SubTask, context: Context) {
        subTask.isCompleted = !subTask.isCompleted
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SUBTASK_IS_COMPLETED, if (subTask.isCompleted) 1 else 0)
        }
        dbHelper.writableDatabase?.update(
            DatabaseHelper.TABLE_SUBTASKS,
            values,
            "${DatabaseHelper.COLUMN_SUBTASK_ID}=?",
            arrayOf(subTask.id.toString())
        )

        if (subTask.isCompleted) {
            cancelNotification(context, subTask.id)
        } else {
            if (!Utils.isReminderInPast(subTask.date, subTask.time)) {
                scheduleNotificationWithSettings(context, subTask)
            }
        }

        val index = reminder.subTasks.indexOf(subTask)
        if (index != -1) {
            reminder.subTasks[index] = subTask.copy(isCompleted = subTask.isCompleted)
        }
    }
    /**
     * Удаляет напоминание и все его подзадачи.
     *
     * @param reminder Напоминание для удаления
     * @param context Контекст приложения
     *
     * @author Грехов М.В., Яньшина А.Ю.
     * @since 1.0.0
     */
    fun removeReminder(reminder: Reminder, context: Context) {
        reminders.remove(reminder)
        dbHelper.writableDatabase?.delete(
            DatabaseHelper.TABLE_REMINDERS,
            "${DatabaseHelper.COLUMN_ID}=?",
            arrayOf(reminder.id.toString())
        )
        cancelNotification(context, reminder.id)
        Toast.makeText(context, R.string.toast_task_removed, Toast.LENGTH_LONG).show()
    }
    /**
     * Удаляет напоминание и все его подзадачи.
     *
     * @param reminder Напоминание для удаления
     * @param context Контекст приложения
     *
     * @author Грехов М.В., Яньшина А.Ю.
     * @since 1.0.0
     */
    fun removeSubTask(reminder: Reminder, subTask: SubTask, context: Context) {
        reminder.subTasks.remove(subTask)
        dbHelper.writableDatabase?.delete(
            DatabaseHelper.TABLE_SUBTASKS,
            "${DatabaseHelper.COLUMN_SUBTASK_ID}=?",
            arrayOf(subTask.id.toString())
        )
        cancelNotification(context, subTask.id)
        Toast.makeText(context, R.string.toast_subtask_removed, Toast.LENGTH_LONG).show()
    }
    // ==================== Загрузка данных из БД ====================

    /**
     * Загружает все напоминания из базы данных.
     *
     * Также загружает подзадачи для каждого напоминания,
     * планирует активные уведомления и удаляет выполненные просроченные задачи.
     *
     * @param context Контекст приложения
     *
     * @author Грехов М.В., Яньшина А.Ю.
     * @since 1.0.0
     */
    fun getReminders(context: Context) {
        try {
            reminders.clear()

            val cursor = dbHelper.readableDatabase?.query(
                DatabaseHelper.TABLE_REMINDERS,
                null, null, null, null, null, null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val idColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)
                    val textColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TEXT)
                    val dateColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)
                    val timeColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME)
                    val completedColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_COMPLETED)
                    val priorityColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRIORITY)

                    val repeatTypeColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REPEAT_TYPE)
                    val selectedDaysColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SELECTED_DAYS)
                    val soundUriColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SOUND_URI)
                    val vibrationColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VIBRATION_ENABLED)
                    val repeatCountColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REPEAT_COUNT)
                    val repeatIntervalColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REPEAT_INTERVAL)

                    do {
                        val id = it.getInt(idColumn)
                        val text = it.getString(textColumn)
                        val date = it.getString(dateColumn)
                        val time = it.getString(timeColumn)
                        val isCompleted = it.getInt(completedColumn) == 1
                        val priority = try {
                            Priority.valueOf(it.getString(priorityColumn))
                        } catch (e: Exception) {
                            Priority.MEDIUM
                        }

                        val repeatType = try {
                            RepeatType.valueOf(it.getString(repeatTypeColumn))
                        } catch (e: Exception) {
                            RepeatType.ONCE
                        }

                        val selectedDaysStr = it.getString(selectedDaysColumn)
                        val selectedDays = if (selectedDaysStr.isNotEmpty()) {
                            selectedDaysStr.split(",").mapNotNull { dayName ->
                                try {
                                    WeekDay.valueOf(dayName)
                                } catch (e: Exception) {
                                    null
                                }
                            }.toSet()
                        } else emptySet()

                        val soundUri = it.getString(soundUriColumn).let { uriStr ->
                            if (uriStr.isNotEmpty()) android.net.Uri.parse(uriStr)
                            else android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
                        }

                        val isVibrationEnabled = it.getInt(vibrationColumn) == 1
                        val repeatCount = it.getInt(repeatCountColumn)
                        val repeatInterval = it.getInt(repeatIntervalColumn)

                        val notificationSettings = NotificationSettings(
                            repeatType = repeatType,
                            selectedDays = selectedDays,
                            soundUri = soundUri,
                            isVibrationEnabled = isVibrationEnabled,
                            repeatCount = repeatCount,
                            repeatIntervalMinutes = repeatInterval
                        )

                        val reminder = Reminder(id, text, date, time, isCompleted, priority, mutableStateListOf(), notificationSettings)
                        loadSubTasks(reminder, context)

                        val isPast = Utils.isReminderInPast(date, time)
// Автоматическое удаление выполненных просроченных задач (добавлено в версии 1.2.2)
                        if (isCompleted && isPast) {
                            val hasActiveSubTasks = reminder.subTasks.any { !it.isCompleted }
                            if (!hasActiveSubTasks) {
                                dbHelper.writableDatabase?.delete(
                                    DatabaseHelper.TABLE_REMINDERS,
                                    "${DatabaseHelper.COLUMN_ID}=?",
                                    arrayOf(reminder.id.toString())
                                )
                                cancelNotification(context, reminder.id)
                            } else {
                                reminders.add(reminder)
                            }
                        } else {
                            reminders.add(reminder)
                            if (!reminder.isCompleted && !isPast) {
                                scheduleNotificationWithSettings(context, reminder)
                            }
                        }

                    } while (it.moveToNext())
                }
            }

            cursor?.close()
            sortReminders()

        } catch (e: Exception) {
            android.util.Log.e("RemindersViewModel", "Error loading reminders", e)
        }
    }
    /**
     * Очищает выполненные и просроченные напоминания.
     *
     * Выполненные задачи с истекшим дедлайном и без активных подзадач
     * автоматически удаляются из базы данных.
     *
     * @param context Контекст приложения
     *
     * @author Яньшина А.Ю.
     * @since 1.2.2
     */
    fun cleanExpiredCompletedReminders(context: Context) {
        val remindersToRemove = reminders.filter { reminder ->
            reminder.isCompleted &&
                    Utils.isReminderInPast(reminder.date, reminder.time) &&
                    reminder.subTasks.none { !it.isCompleted }
        }.toList()

        remindersToRemove.forEach { reminder ->
            removeReminder(reminder, context)
        }

        reminders.forEach { reminder ->
            val subtasksToRemove = reminder.subTasks.filter { subTask ->
                subTask.isCompleted && Utils.isReminderInPast(subTask.date, subTask.time)
            }.toList()

            subtasksToRemove.forEach { subTask ->
                removeSubTask(reminder, subTask, context)
            }
        }
    }
    /**
     * Загружает подзадачи для указанного напоминания из базы данных.
     *
     * @param reminder Напоминание, для которого загружаются подзадачи
     * @param context Контекст приложения
     *
     * @author Яньшина А.Ю.
     * @since 1.2.0
     */
    private fun loadSubTasks(reminder: Reminder, context: Context) {
        val cursor = dbHelper.readableDatabase?.query(
            DatabaseHelper.TABLE_SUBTASKS,
            null,
            "${DatabaseHelper.COLUMN_PARENT_ID}=?",
            arrayOf(reminder.id.toString()),
            null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val idColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTASK_ID)
                val textColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTASK_TEXT)
                val dateColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTASK_DATE)
                val timeColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTASK_TIME)
                val completedColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTASK_IS_COMPLETED)
                val priorityColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTASK_PRIORITY)

                val repeatTypeColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTASK_REPEAT_TYPE)
                val selectedDaysColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTASK_SELECTED_DAYS)
                val soundUriColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTASK_SOUND_URI)
                val vibrationColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTASK_VIBRATION_ENABLED)
                val repeatCountColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTASK_REPEAT_COUNT)
                val repeatIntervalColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTASK_REPEAT_INTERVAL)

                do {
                    val id = it.getInt(idColumn)
                    val text = it.getString(textColumn)
                    val date = it.getString(dateColumn)
                    val time = it.getString(timeColumn)
                    val isCompleted = it.getInt(completedColumn) == 1
                    val priority = try {
                        Priority.valueOf(it.getString(priorityColumn))
                    } catch (e: Exception) {
                        Priority.MEDIUM
                    }

                    val repeatType = try {
                        RepeatType.valueOf(it.getString(repeatTypeColumn))
                    } catch (e: Exception) {
                        RepeatType.ONCE
                    }

                    val selectedDaysStr = it.getString(selectedDaysColumn)
                    val selectedDays = if (selectedDaysStr.isNotEmpty()) {
                        selectedDaysStr.split(",").mapNotNull { dayName ->
                            try {
                                WeekDay.valueOf(dayName)
                            } catch (e: Exception) {
                                null
                            }
                        }.toSet()
                    } else emptySet()

                    val soundUri = it.getString(soundUriColumn).let { uriStr ->
                        if (uriStr.isNotEmpty()) android.net.Uri.parse(uriStr)
                        else android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
                    }

                    val isVibrationEnabled = it.getInt(vibrationColumn) == 1
                    val repeatCount = it.getInt(repeatCountColumn)
                    val repeatInterval = it.getInt(repeatIntervalColumn)

                    val notificationSettings = NotificationSettings(
                        repeatType = repeatType,
                        selectedDays = selectedDays,
                        soundUri = soundUri,
                        isVibrationEnabled = isVibrationEnabled,
                        repeatCount = repeatCount,
                        repeatIntervalMinutes = repeatInterval
                    )

                    val subTask = SubTask(id, text, date, time, isCompleted, priority, notificationSettings)

                    val isPast = Utils.isReminderInPast(date, time)

                    if (isCompleted && isPast) {
                        dbHelper.writableDatabase?.delete(
                            DatabaseHelper.TABLE_SUBTASKS,
                            "${DatabaseHelper.COLUMN_SUBTASK_ID}=?",
                            arrayOf(id.toString())
                        )
                        cancelNotification(context, id)
                    } else {
                        reminder.subTasks.add(subTask)
                    }

                } while (it.moveToNext())
            }
        }
    }
    // ==================== Сортировка ====================

    /**
     * Сортирует список напоминаний и подзадач.
     *
     * Сортировка происходит сначала по приоритету (HIGH > MEDIUM > LOW),
     * затем по дате и времени (ближайшие задачи выше).
     *
     * @author Грехов М.В. (приоритеты), Яньшина А.Ю. (подзадачи)
     * @since 1.1.0 (приоритеты), 1.2.0 (сортировка подзадач)
     */
    fun sortReminders() {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        reminders.sortWith(compareByDescending<Reminder> { Utils.getPriorityValue(it.priority) }
            .thenBy {
                try {
                    LocalDateTime.parse("${it.date} ${it.time}", formatter)
                } catch (e: Exception) {
                    LocalDateTime.MAX
                }
            })

        reminders.forEach { reminder ->
            reminder.subTasks.sortWith(compareByDescending<SubTask> { Utils.getPriorityValue(it.priority) }
                .thenBy {
                    try {
                        LocalDateTime.parse("${it.date} ${it.time}", formatter)
                    } catch (e: Exception) {
                        LocalDateTime.MAX
                    }
                })
        }
    }
    // ==================== Управление уведомлениями ====================

    /**
     * Планирует уведомление для напоминания с учётом настроек повторения.
     *
     * Поддерживает три типа повтора:
     * - ONCE: однократное уведомление
     * - DAILY: ежедневное уведомление
     * - WEEKLY: еженедельное уведомление по выбранным дням
     *
     * @param context Контекст приложения
     * @param reminder Напоминание для планирования
     *
     * @author Яньшина А.Ю.
     * @since 2.0.0
     */
    fun scheduleNotificationWithSettings(context: Context, reminder: Reminder) {
        if (reminder.isCompleted || Utils.isReminderInPast(reminder.date, reminder.time)) {
            return
        }

        val settings = reminder.notificationSettings

        when (settings.repeatType) {
            RepeatType.ONCE -> {
                scheduleSingleNotification(context, reminder.id, reminder.text, reminder.date, reminder.time, settings)
            }
            RepeatType.DAILY -> {
                scheduleDailyNotifications(context, reminder.id, reminder.text, reminder.time, settings)
            }
            RepeatType.WEEKLY -> {
                scheduleWeeklyNotifications(context, reminder.id, reminder.text, reminder.time, settings)
            }
        }
    }
    /**
     * Планирует уведомление для подзадачи с учётом настроек повторения.
     *
     * @param context Контекст приложения
     * @param subTask Подзадача для планирования
     *
     * @author Яньшина А.Ю.
     * @since 2.0.0
     */
    fun scheduleNotificationWithSettings(context: Context, subTask: SubTask) {
        if (subTask.isCompleted || Utils.isReminderInPast(subTask.date, subTask.time)) {
            return
        }

        val settings = subTask.notificationSettings

        when (settings.repeatType) {
            RepeatType.ONCE -> {
                scheduleSingleNotification(context, subTask.id, subTask.text, subTask.date, subTask.time, settings)
            }
            RepeatType.DAILY -> {
                scheduleDailyNotifications(context, subTask.id, subTask.text, subTask.time, settings)
            }
            RepeatType.WEEKLY -> {
                scheduleWeeklyNotifications(context, subTask.id, subTask.text, subTask.time, settings)
            }
        }
    }
    /**
     * Планирует однократное уведомление.
     *
     * @param context Контекст приложения
     * @param id ID уведомления
     * @param text Текст уведомления
     * @param date Дата срабатывания
     * @param time Время срабатывания
     * @param settings Настройки уведомления
     *
     * @author Яньшина А.Ю.
     * @since 2.0.0
     */
    private fun scheduleSingleNotification(context: Context, id: Int, text: String, date: String, time: String, settings: NotificationSettings) {
        val dateTime = "$date $time"
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val triggerTime = sdf.parse(dateTime)?.time ?: return

        if (triggerTime <= System.currentTimeMillis()) return

        scheduleNotification(context, id, text, triggerTime, settings)

        if (settings.repeatCount > 0) {
            scheduleRepeatedNotifications(context, id, text, triggerTime, settings)
        }
    }
    /**
     * Планирует ежедневные повторяющиеся уведомления.
     *
     * @param context Контекст приложения
     * @param id ID уведомления
     * @param text Текст уведомления
     * @param time Время срабатывания (HH:mm)
     * @param settings Настройки уведомления
     *
     * @author Яньшина А.Ю.
     * @since 2.0.0
     */
    private fun scheduleDailyNotifications(context: Context, id: Int, text: String, time: String, settings: NotificationSettings) {
        val calendar = java.util.Calendar.getInstance()
        val timeParts = time.split(":")
        calendar.set(java.util.Calendar.HOUR_OF_DAY, timeParts[0].toInt())
        calendar.set(java.util.Calendar.MINUTE, timeParts[1].toInt())
        calendar.set(java.util.Calendar.SECOND, 0)

        var triggerTime = calendar.timeInMillis
        if (triggerTime <= System.currentTimeMillis()) {
            triggerTime += 24 * 60 * 60 * 1000
        }

        scheduleRepeatingNotification(context, id, text, triggerTime, 24 * 60 * 60 * 1000, settings)
    }
    /**
     * Планирует еженедельные повторяющиеся уведомления.
     *
     * Уведомления планируются на каждый выбранный день недели.
     *
     * @param context Контекст приложения
     * @param id ID уведомления
     * @param text Текст уведомления
     * @param time Время срабатывания (HH:mm)
     * @param settings Настройки уведомления (содержит выбранные дни)
     *
     * @author Яньшина А.Ю.
     * @since 2.0.0
     */
    private fun scheduleWeeklyNotifications(context: Context, id: Int, text: String, time: String, settings: NotificationSettings) {
        val timeParts = time.split(":")

        settings.selectedDays.forEach { day ->
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.DAY_OF_WEEK, day.value)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            calendar.set(java.util.Calendar.MINUTE, timeParts[1].toInt())
            calendar.set(java.util.Calendar.SECOND, 0)

            var triggerTime = calendar.timeInMillis
            if (triggerTime <= System.currentTimeMillis()) {
                triggerTime += 7 * 24 * 60 * 60 * 1000
            }

            scheduleRepeatingNotification(context, id, text, triggerTime, 7 * 24 * 60 * 60 * 1000, settings)
        }
    }
    /**
     * Планирует повторяющееся уведомление с указанным интервалом.
     *
     * @param context Контекст приложения
     * @param id ID уведомления
     * @param text Текст уведомления
     * @param triggerTime Время первого срабатывания
     * @param interval Интервал между срабатываниями в миллисекундах
     * @param settings Настройки уведомления
     *
     * @author Яньшина А.Ю.
     * @since 2.0.0
     */
    private fun scheduleRepeatingNotification(context: Context, id: Int, text: String, triggerTime: Long, interval: Long, settings: NotificationSettings) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pendingIntent = Utils.getPendingIntentWithSettings(context, id, text, settings)
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                interval,
                pendingIntent
            )
        }
    }

    /**
     * Планирует однократное точное уведомление.
     *
     * @param context Контекст приложения
     * @param id ID уведомления
     * @param text Текст уведомления
     * @param triggerTime Время срабатывания в миллисекундах
     * @param settings Настройки уведомления
     *
     * @author Яньшина А.Ю.
     * @since 2.0.0
     */
    private fun scheduleNotification(context: Context, id: Int, text: String, triggerTime: Long, settings: NotificationSettings) {
        // Логируем перед отправкой
        android.util.Log.d("RemindersViewModel", "Scheduling notification with sound: ${settings.soundUri}")

        val pendingIntent = Utils.getPendingIntentWithSettings(context, id, text, settings)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
    /**
     * Планирует повторяющиеся уведомления (дополнительные повторы).
     *
     * Используется для создания нескольких уведомлений с интервалом
     * после основного (например, напоминание через 5, 10, 15 минут).
     *
     * @param context Контекст приложения
     * @param id ID уведомления
     * @param text Текст уведомления
     * @param firstTriggerTime Время первого срабатывания
     * @param settings Настройки уведомления (содержит количество повторов и интервал)
     *
     * @author Яньшина А.Ю.
     * @since 2.0.0
     */
    private fun scheduleRepeatedNotifications(context: Context, id: Int, text: String, firstTriggerTime: Long, settings: NotificationSettings) {
        for (i in 1..settings.repeatCount) {
            val repeatTime = firstTriggerTime + (i * settings.repeatIntervalMinutes * 60 * 1000)
            if (repeatTime > System.currentTimeMillis()) {
                val repeatId = "$id-$i".hashCode()
                val pendingIntent = Utils.getPendingIntentWithSettings(context, repeatId, text, settings)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, repeatTime, pendingIntent)
                    }
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, repeatTime, pendingIntent)
                }
            }
        }
    }
    /**
     * Проверяет, является ли указанная дата сегодняшним днём.
     *
     * Используется для валидации времени при создании задач.
     *
     * @param date Дата в формате dd.MM.yyyy
     * @return true, если дата совпадает с сегодняшней
     *
     * @author Грехов М.В.
     * @since 1.2.1
     */
    private fun isTodayForMain(date: String): Boolean {
        if (date.isEmpty()) return false

        try {

            val dayStr = date.substringBefore(".")
            val remaining = date.substringAfter(".")
            val monthStr = remaining.substringBefore(".")
            val yearStr = remaining.substringAfter(".")

            val day = dayStr.toIntOrNull() ?: return false
            val month = monthStr.toIntOrNull() ?: return false
            val year = yearStr.toIntOrNull() ?: return false

            val calendar = Calendar.getInstance()
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            val currentMonth = calendar.get(Calendar.MONTH) + 1
            val currentYear = calendar.get(Calendar.YEAR)

            return day == currentDay && month == currentMonth && year == currentYear
        } catch (e: Exception) {
            return false
        }
    }
    /**
     * Отменяет запланированное уведомление.
     *
     * @param context Контекст приложения
     * @param id ID уведомления для отмены
     *
     * @author Грехов М.В., Яньшина А.Ю.
     * @since 1.0.0
     */
    fun cancelNotification(context: Context, id: Int) {
        val pendingIntent = Utils.getPendingIntent(context, id, "")
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}