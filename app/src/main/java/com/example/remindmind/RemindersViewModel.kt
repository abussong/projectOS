//package com.example.remindmind
//
//import android.app.AlarmManager
//import android.content.ContentValues
//import android.content.Context
//import android.os.Build
//import android.widget.Toast
//import androidx.annotation.RequiresApi
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import org.threeten.bp.LocalDateTime
//import org.threeten.bp.format.DateTimeFormatter
//import java.text.SimpleDateFormat
//import java.util.Locale
//
//class RemindersViewModel: ViewModel() {
//    lateinit var dbHelper: DatabaseHelper
//    lateinit var alarmManager: AlarmManager
//
//    var text by mutableStateOf("")
//    var date by mutableStateOf("")
//    var time by mutableStateOf("")
//
//    var reminders = mutableStateListOf<Reminder>()
//        private set
//
//    fun addReminder(context: Context) {
//        if(date.isEmpty() && time.isEmpty()) {
//            return Toast.makeText(context, R.string.toast_datetime_error, Toast.LENGTH_LONG).show()
//        } else if(text.isEmpty()) {
//            return Toast.makeText(context, R.string.toast_text_error, Toast.LENGTH_LONG).show()
//        }
//
//        if(date.isEmpty()) date = Utils.getCurrentDate()
//        if(time.isEmpty()) time = "12:00"
//
//        val reminder = Reminder(Utils.getID(), text, date, time)
//        reminders.add(reminder)
//
//        dbHelper.writableDatabase?.insert(DatabaseHelper.TABLE_NAME, null, ContentValues().apply {
//            put(DatabaseHelper.COLUMN_ID, reminder.id)
//            put(DatabaseHelper.COLUMN_TEXT, reminder.text)
//            put(DatabaseHelper.COLUMN_DATE, reminder.date)
//            put(DatabaseHelper.COLUMN_TIME, reminder.time)
//        })
//
//        text = ""
//        date = ""
//        time = ""
//
//        scheduleNotification(context, reminder.date, reminder.time, reminder.text, reminder.id)
//        sortReminders()
//        Toast.makeText(context, R.string.toast_task_created, Toast.LENGTH_LONG).show()
//    }
//
//    fun removeReminder(reminder: Reminder, context: Context) {
//        reminders.remove(reminder)
//        dbHelper.writableDatabase?.delete(DatabaseHelper.TABLE_NAME, "${DatabaseHelper.COLUMN_ID}=?", arrayOf(reminder.id.toString()))
//        alarmManager.cancel(Utils.getPendingIntent(context, reminder.id, reminder.text))
//        Toast.makeText(context, R.string.toast_task_removed, Toast.LENGTH_LONG).show()
//    }
//
//    fun getReminders(context: Context) {
//        reminders.clear()
//        val cursor = dbHelper.readableDatabase?.query(
//            DatabaseHelper.TABLE_NAME,
//            null, null, null, null, null, null
//        )
//
//        if (cursor?.moveToFirst() == true) {
//            //получаем индексы колонок один раз
//            val idColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)
//            val textColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TEXT)
//            val dateColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE)
//            val timeColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TIME)
//
//            //проверяем, что все колонки существуют
//            if (idColumnIndex == -1 || textColumnIndex == -1 ||
//                dateColumnIndex == -1 || timeColumnIndex == -1) {
//                cursor.close()
//                return
//            }
//
//            do {
//                val id = cursor.getInt(idColumnIndex)
//                val text = cursor.getString(textColumnIndex)
//                val date = cursor.getString(dateColumnIndex)
//                val time = cursor.getString(timeColumnIndex)
//
//                val reminder = Reminder(id, text, date, time)
//                if (Utils.isReminderInPast(date, time)) {
//                    removeReminder(reminder, context)
//                } else {
//                    reminders.add(reminder)
//                }
//            } while (cursor.moveToNext())
//        }
//        cursor?.close()
//        sortReminders()
//    }
//
//    fun sortReminders() {
//        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
//        reminders.sortWith(compareBy { reminder ->
//            LocalDateTime.parse("${reminder.date} ${reminder.time}", formatter)
//        })
//    }
//
//    fun scheduleNotification(context: Context, date: String, time: String, text: String, id: Int) {
//        val dateTime = "$date $time"
//        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
//        val triggerRime = sdf.parse(dateTime)?.time ?: return
//        val pendingIntent = Utils.getPendingIntent(context, id, text)
//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerRime, pendingIntent)
//        }
//    }
//}
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

        scheduleNotification(context, reminder.date, reminder.time, reminder.text, reminder.id)
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

        // Добавляем подзадачу в наблюдаемый список
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

        scheduleNotification(context, subTask.date, subTask.time, subTask.text, subTask.id)

        subTaskText = ""
        subTaskDate = ""
        subTaskTime = ""
        subTaskPriority = Priority.MEDIUM
        showSubTaskDialog = false

        sortReminders()
        Toast.makeText(context, R.string.toast_subtask_created, Toast.LENGTH_LONG).show()
    }

    fun toggleReminderCompletion(reminder: Reminder) {
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
        sortReminders()

        // Принудительно обновляем список, чтобы Compose перерисовал
        val index = reminders.indexOf(reminder)
        if (index != -1) {
            reminders[index] = reminder.copy(isCompleted = reminder.isCompleted)
        }
    }

    fun toggleSubTaskCompletion(reminder: Reminder, subTask: SubTask) {
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

        // Обновляем UI - находим подзадачу и заменяем её
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
        alarmManager.cancel(Utils.getPendingIntent(context, reminder.id, reminder.text))
        Toast.makeText(context, R.string.toast_task_removed, Toast.LENGTH_LONG).show()
    }

    fun removeSubTask(reminder: Reminder, subTask: SubTask, context: Context) {
        reminder.subTasks.remove(subTask)
        dbHelper.writableDatabase?.delete(
            DatabaseHelper.TABLE_SUBTASKS,
            "${DatabaseHelper.COLUMN_SUBTASK_ID}=?",
            arrayOf(subTask.id.toString())
        )
        alarmManager.cancel(Utils.getPendingIntent(context, subTask.id, subTask.text))
        Toast.makeText(context, R.string.toast_subtask_removed, Toast.LENGTH_LONG).show()
    }

    fun getReminders(context: Context) {
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
                    loadSubTasks(reminder)

                    if (!reminder.isCompleted && Utils.isReminderInPast(date, time)) {
                        removeReminder(reminder, context)
                    } else {
                        reminders.add(reminder)
                    }
                } while (it.moveToNext())
            }
        }
        sortReminders()
    }

    private fun loadSubTasks(reminder: Reminder) {
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

                    reminder.subTasks.add(SubTask(id, text, date, time, isCompleted, priority))
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
        val dateTime = "$date $time"
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val triggerTime = sdf.parse(dateTime)?.time ?: return
        val pendingIntent = Utils.getPendingIntent(context, id, text)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
}