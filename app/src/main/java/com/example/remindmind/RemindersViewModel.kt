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

class RemindersViewModel : ViewModel() {
    lateinit var dbHelper: DatabaseHelper
    lateinit var alarmManager: AlarmManager

    var text by mutableStateOf("")
    var date by mutableStateOf("")
    var time by mutableStateOf("")
    var selectedPriority by mutableStateOf(Priority.MEDIUM)
    var tempNotificationSettings by mutableStateOf(NotificationSettings())

    var showSubTaskDialog by mutableStateOf(false)
    var currentReminderForSubTask: Reminder? by mutableStateOf(null)
    var subTaskText by mutableStateOf("")
    var subTaskDate by mutableStateOf("")
    var subTaskTime by mutableStateOf("")
    var subTaskPriority by mutableStateOf(Priority.MEDIUM)
    var subTaskNotificationSettings by mutableStateOf(NotificationSettings())

    val reminders = mutableStateListOf<Reminder>()
    fun editReminderText(reminder: Reminder, newText: String, context: Context) {
        if (newText.isBlank()) return

        // Отменяем старые уведомления
        cancelNotification(context, reminder.id)

        // Обновляем текст в объекте
        reminder.text = newText

        // Обновляем в базе данных
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

        // Обновляем в списке reminders
        val index = reminders.indexOf(reminder)
        if (index != -1) {
            reminders[index] = reminder.copy(text = newText, isCompleted = reminder.isCompleted)
        }

        sortReminders()
        Toast.makeText(context, R.string.toast_task_updated, Toast.LENGTH_LONG).show()
    }

    fun editSubTaskText(reminder: Reminder, subTask: SubTask, newText: String, context: Context) {
        if (newText.isBlank()) return

        // Отменяем старые уведомления подзадачи
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

        // ФИНАЛЬНАЯ ПРОВЕРКА: если дата сегодня, проверяем что время не в прошлом
        if (isTodayForMain(finalDate)) {
            val currentCalendar = Calendar.getInstance()
            val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = currentCalendar.get(Calendar.MINUTE)

            val timeParts = finalTime.split(":")
            val selectedHour = timeParts[0].toInt()
            val selectedMinute = timeParts[1].toInt()

            // Если время уже прошло
            if (selectedHour < currentHour || (selectedHour == currentHour && selectedMinute < currentMinute)) {
                Toast.makeText(context, R.string.time_cannot_be_past, Toast.LENGTH_LONG).show()
                return  // Не создаем задачу
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

    fun cancelNotification(context: Context, id: Int) {
        val pendingIntent = Utils.getPendingIntent(context, id, "")
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}