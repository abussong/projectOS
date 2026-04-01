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
import java.util.Locale

class RemindersViewModel : ViewModel() {
    lateinit var dbHelper: DatabaseHelper
    lateinit var alarmManager: AlarmManager

    var text by mutableStateOf("")
    var date by mutableStateOf("")
    var time by mutableStateOf("")
    var selectedPriority by mutableStateOf(Priority.MEDIUM)

    var showSubTaskDialog by mutableStateOf(false)
    var currentReminderForSubTask: Reminder? by mutableStateOf(null)
    var subTaskText by mutableStateOf("")
    var subTaskDate by mutableStateOf("")
    var subTaskTime by mutableStateOf("")
    var subTaskPriority by mutableStateOf(Priority.MEDIUM)

    val reminders = mutableStateListOf<Reminder>()

    fun addReminder(context: Context) {
        if (date.isEmpty() && time.isEmpty()) {
            Toast.makeText(context, R.string.toast_datetime_error, Toast.LENGTH_LONG).show()
            return
        } else if (text.isEmpty()) {
            Toast.makeText(context, R.string.toast_text_error, Toast.LENGTH_LONG).show()
            return
        }

        val finalDate = if (date.isEmpty()) Utils.getCurrentDate() else date
        val finalTime = if (time.isEmpty()) "12:00" else time

        val reminder = Reminder(
            id = Utils.getID(),
            text = text,
            date = finalDate,
            time = finalTime,
            priority = selectedPriority
        )
        reminders.add(reminder)

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_ID, reminder.id)
            put(DatabaseHelper.COLUMN_TEXT, reminder.text)
            put(DatabaseHelper.COLUMN_DATE, reminder.date)
            put(DatabaseHelper.COLUMN_TIME, reminder.time)
            put(DatabaseHelper.COLUMN_IS_COMPLETED, if (reminder.isCompleted) 1 else 0)
            put(DatabaseHelper.COLUMN_PRIORITY, reminder.priority.name)
        }
        dbHelper.writableDatabase?.insert(DatabaseHelper.TABLE_REMINDERS, null, values)

        text = ""
        date = ""
        time = ""
        selectedPriority = Priority.MEDIUM

        //Планируем уведомление только если задача не выполнена
        if (!reminder.isCompleted && !Utils.isReminderInPast(reminder.date, reminder.time)) {
            scheduleNotification(context, reminder.date, reminder.time, reminder.text, reminder.id)
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
            priority = subTaskPriority
        )

        //Добавляем подзадачу в наблюдаемый список
        parentReminder.subTasks.add(subTask)

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SUBTASK_ID, subTask.id)
            put(DatabaseHelper.COLUMN_PARENT_ID, parentReminder.id)
            put(DatabaseHelper.COLUMN_SUBTASK_TEXT, subTask.text)
            put(DatabaseHelper.COLUMN_SUBTASK_DATE, subTask.date)
            put(DatabaseHelper.COLUMN_SUBTASK_TIME, subTask.time)
            put(DatabaseHelper.COLUMN_SUBTASK_IS_COMPLETED, if (subTask.isCompleted) 1 else 0)
            put(DatabaseHelper.COLUMN_SUBTASK_PRIORITY, subTask.priority.name)
        }
        dbHelper.writableDatabase?.insert(DatabaseHelper.TABLE_SUBTASKS, null, values)

        //Планируем уведомление только если подзадача не выполнена
        if (!subTask.isCompleted && !Utils.isReminderInPast(subTask.date, subTask.time)) {
            scheduleNotification(context, subTask.date, subTask.time, subTask.text, subTask.id)
        }

        subTaskText = ""
        subTaskDate = ""
        subTaskTime = ""
        subTaskPriority = Priority.MEDIUM
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

        //Управляем уведомлением в зависимости от состояния
        if (reminder.isCompleted) {
            //Если задача выполнена - отменяем уведомление
            cancelNotification(context, reminder.id)
        } else {
            //Если задачу снова отметили как невыполненную и время не прошло - планируем уведомление
            if (!Utils.isReminderInPast(reminder.date, reminder.time)) {
                scheduleNotification(
                    context,
                    reminder.date,
                    reminder.time,
                    reminder.text,
                    reminder.id
                )
            }
        }

        sortReminders()

        //Принудительно обновляем список, чтобы Compose перерисовал
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

        //Управляем уведомлением в зависимости от состояния
        if (subTask.isCompleted) {
            //Если подзадача выполнена - отменяем уведомление
            cancelNotification(context, subTask.id)
        } else {
            //Если подзадачу снова отметили как невыполненную и время не прошло - планируем уведомление
            if (!Utils.isReminderInPast(subTask.date, subTask.time)) {
                scheduleNotification(context, subTask.date, subTask.time, subTask.text, subTask.id)
            }
        }

        //Обновляем UI - находим подзадачу и заменяем её
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
                    val completedColumn =
                        it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_COMPLETED)
                    val priorityColumn = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRIORITY)

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

                        val reminder = Reminder(id, text, date, time, isCompleted, priority)
                        loadSubTasks(reminder, context) //Передаем context

                        //Проверяем, просрочена ли задача
                        val isPast = Utils.isReminderInPast(date, time)

                        //УДАЛЯЕМ ТОЛЬКО ВЫПОЛНЕННЫЕ И ПРОСРОЧЕННЫЕ ЗАДАЧИ
                        if (isCompleted && isPast) {
                            //Проверяем, есть ли у задачи невыполненные подзадачи
                            val hasActiveSubTasks = reminder.subTasks.any { !it.isCompleted }

                            //Если нет активных подзадач - удаляем задачу
                            if (!hasActiveSubTasks) {
                                android.util.Log.d(
                                    "RemindersViewModel",
                                    "Removing completed and expired reminder: $text"
                                )
                                dbHelper.writableDatabase?.delete(
                                    DatabaseHelper.TABLE_REMINDERS,
                                    "${DatabaseHelper.COLUMN_ID}=?",
                                    arrayOf(reminder.id.toString())
                                )
                                cancelNotification(context, reminder.id)
                                //Не добавляем в список
                            } else {
                                //Если есть активные подзадачи - добавляем задачу
                                android.util.Log.d(
                                    "RemindersViewModel",
                                    "Keeping reminder with active subtasks: $text"
                                )
                                reminders.add(reminder)
                            }
                        } else {
                            //Все остальные задачи добавляем в список
                            reminders.add(reminder)

                            //Планируем уведомление только для невыполненных и непросроченных задач
                            if (!reminder.isCompleted && !isPast) {
                                scheduleNotification(
                                    context,
                                    reminder.date,
                                    reminder.time,
                                    reminder.text,
                                    reminder.id
                                )
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

    //метод для очистки ВЫПОЛНЕННЫХ просроченных задач
    fun cleanExpiredCompletedReminders(context: Context) {
        //Собираем выполненные просроченные задачи без активных подзадач
        val remindersToRemove = reminders.filter { reminder ->
            reminder.isCompleted &&
                    Utils.isReminderInPast(reminder.date, reminder.time) &&
                    reminder.subTasks.none { !it.isCompleted }
        }.toList()

        android.util.Log.d(
            "RemindersViewModel",
            "Cleaning expired completed reminders: ${remindersToRemove.size}"
        )

        remindersToRemove.forEach { reminder ->
            android.util.Log.d(
                "RemindersViewModel",
                "Removing expired completed reminder: ${reminder.text}"
            )
            removeReminder(reminder, context)
        }

        //Также удаляем выполненные просроченные подзадачи
        reminders.forEach { reminder ->
            val subtasksToRemove = reminder.subTasks.filter { subTask ->
                subTask.isCompleted && Utils.isReminderInPast(subTask.date, subTask.time)
            }.toList()

            subtasksToRemove.forEach { subTask ->
                android.util.Log.d(
                    "RemindersViewModel",
                    "Removing expired completed subtask: ${subTask.text}"
                )
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
                val completedColumn =
                    it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTASK_IS_COMPLETED)
                val priorityColumn =
                    it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBTASK_PRIORITY)

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

                    val subTask = SubTask(id, text, date, time, isCompleted, priority)

                    //Проверяем, нужно ли удалить подзадачу (выполнена и просрочена)
                    val isPast = Utils.isReminderInPast(date, time)

                    if (isCompleted && isPast) {
                        //Удаляем выполненную просроченную подзадачу
                        android.util.Log.d(
                            "RemindersViewModel",
                            "Removing completed and expired subtask: $text"
                        )
                        dbHelper.writableDatabase?.delete(
                            DatabaseHelper.TABLE_SUBTASKS,
                            "${DatabaseHelper.COLUMN_SUBTASK_ID}=?",
                            arrayOf(id.toString())
                        )
                        cancelNotification(context, id)
                    } else {
                        //Добавляем подзадачу
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

    fun scheduleNotification(context: Context, date: String, time: String, text: String, id: Int) {
        //Не планируем уведомление, если время уже прошло
        if (Utils.isReminderInPast(date, time)) {
            return
        }

        val dateTime = "$date $time"
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val triggerTime = sdf.parse(dateTime)?.time ?: return

        //Проверяем, что время в будущем
        if (triggerTime <= System.currentTimeMillis()) {
            return
        }

        val pendingIntent = Utils.getPendingIntent(context, id, text)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelNotification(context: Context, id: Int) {
        val pendingIntent = Utils.getPendingIntent(context, id, "")
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}