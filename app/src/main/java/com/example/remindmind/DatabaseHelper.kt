package com.example.remindmind

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.provider.Settings

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "reminders.db"
        private const val DATABASE_VERSION = 6

        const val TABLE_REMINDERS = "reminders"
        const val COLUMN_ID = "id"
        const val COLUMN_TEXT = "text"
        const val COLUMN_DATE = "date"
        const val COLUMN_TIME = "time"
        const val COLUMN_IS_COMPLETED = "is_completed"
        const val COLUMN_PRIORITY = "priority"

        // Новые колонки для напоминаний
        const val COLUMN_REPEAT_TYPE = "repeat_type"
        const val COLUMN_SELECTED_DAYS = "selected_days"
        const val COLUMN_SOUND_URI = "sound_uri"
        const val COLUMN_VIBRATION_ENABLED = "vibration_enabled"
        const val COLUMN_REPEAT_COUNT = "repeat_count"
        const val COLUMN_REPEAT_INTERVAL = "repeat_interval"

        const val TABLE_SUBTASKS = "subtasks"
        const val COLUMN_SUBTASK_ID = "subtask_id"
        const val COLUMN_PARENT_ID = "parent_id"
        const val COLUMN_SUBTASK_TEXT = "subtask_text"
        const val COLUMN_SUBTASK_DATE = "subtask_date"
        const val COLUMN_SUBTASK_TIME = "subtask_time"
        const val COLUMN_SUBTASK_IS_COMPLETED = "subtask_is_completed"
        const val COLUMN_SUBTASK_PRIORITY = "subtask_priority"

        // Новые колонки для подзадач
        const val COLUMN_SUBTASK_REPEAT_TYPE = "subtask_repeat_type"
        const val COLUMN_SUBTASK_SELECTED_DAYS = "subtask_selected_days"
        const val COLUMN_SUBTASK_SOUND_URI = "subtask_sound_uri"
        const val COLUMN_SUBTASK_VIBRATION_ENABLED = "subtask_vibration_enabled"
        const val COLUMN_SUBTASK_REPEAT_COUNT = "subtask_repeat_count"
        const val COLUMN_SUBTASK_REPEAT_INTERVAL = "subtask_repeat_interval"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createRemindersTable = """
            CREATE TABLE $TABLE_REMINDERS(
                $COLUMN_ID INTEGER PRIMARY KEY,
                $COLUMN_TEXT TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_TIME TEXT,
                $COLUMN_IS_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_PRIORITY TEXT DEFAULT 'MEDIUM',
                $COLUMN_REPEAT_TYPE TEXT DEFAULT 'ONCE',
                $COLUMN_SELECTED_DAYS TEXT DEFAULT '',
                $COLUMN_SOUND_URI TEXT DEFAULT '',
                $COLUMN_VIBRATION_ENABLED INTEGER DEFAULT 1,
                $COLUMN_REPEAT_COUNT INTEGER DEFAULT 0,
                $COLUMN_REPEAT_INTERVAL INTEGER DEFAULT 5
            )
        """.trimIndent()

        val createSubtasksTable = """
            CREATE TABLE $TABLE_SUBTASKS(
                $COLUMN_SUBTASK_ID INTEGER PRIMARY KEY,
                $COLUMN_PARENT_ID INTEGER,
                $COLUMN_SUBTASK_TEXT TEXT,
                $COLUMN_SUBTASK_DATE TEXT,
                $COLUMN_SUBTASK_TIME TEXT,
                $COLUMN_SUBTASK_IS_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_SUBTASK_PRIORITY TEXT DEFAULT 'MEDIUM',
                $COLUMN_SUBTASK_REPEAT_TYPE TEXT DEFAULT 'ONCE',
                $COLUMN_SUBTASK_SELECTED_DAYS TEXT DEFAULT '',
                $COLUMN_SUBTASK_SOUND_URI TEXT DEFAULT '',
                $COLUMN_SUBTASK_VIBRATION_ENABLED INTEGER DEFAULT 1,
                $COLUMN_SUBTASK_REPEAT_COUNT INTEGER DEFAULT 0,
                $COLUMN_SUBTASK_REPEAT_INTERVAL INTEGER DEFAULT 5,
                FOREIGN KEY($COLUMN_PARENT_ID) REFERENCES $TABLE_REMINDERS($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db?.execSQL(createRemindersTable)
        db?.execSQL(createSubtasksTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SUBTASKS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_REMINDERS")
        onCreate(db)
    }
}
/*package com.example.remindmind

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "reminders.db"
        private const val DATABASE_VERSION = 5  // Увеличиваем версию для пересоздания

        const val TABLE_REMINDERS = "reminders"
        const val COLUMN_ID = "id"
        const val COLUMN_TEXT = "text"
        const val COLUMN_DATE = "date"
        const val COLUMN_TIME = "time"
        const val COLUMN_IS_COMPLETED = "is_completed"
        const val COLUMN_PRIORITY = "priority"

        const val TABLE_SUBTASKS = "subtasks"
        const val COLUMN_SUBTASK_ID = "subtask_id"
        const val COLUMN_PARENT_ID = "parent_id"
        const val COLUMN_SUBTASK_TEXT = "subtask_text"
        const val COLUMN_SUBTASK_DATE = "subtask_date"
        const val COLUMN_SUBTASK_TIME = "subtask_time"
        const val COLUMN_SUBTASK_IS_COMPLETED = "subtask_is_completed"
        const val COLUMN_SUBTASK_PRIORITY = "subtask_priority"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createRemindersTable = """
            CREATE TABLE $TABLE_REMINDERS(
                $COLUMN_ID INTEGER PRIMARY KEY,
                $COLUMN_TEXT TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_TIME TEXT,
                $COLUMN_IS_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_PRIORITY TEXT DEFAULT 'MEDIUM'
            )
        """.trimIndent()

        val createSubtasksTable = """
            CREATE TABLE $TABLE_SUBTASKS(
                $COLUMN_SUBTASK_ID INTEGER PRIMARY KEY,
                $COLUMN_PARENT_ID INTEGER,
                $COLUMN_SUBTASK_TEXT TEXT,
                $COLUMN_SUBTASK_DATE TEXT,
                $COLUMN_SUBTASK_TIME TEXT,
                $COLUMN_SUBTASK_IS_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_SUBTASK_PRIORITY TEXT DEFAULT 'MEDIUM',
                FOREIGN KEY($COLUMN_PARENT_ID) REFERENCES $TABLE_REMINDERS($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db?.execSQL(createRemindersTable)
        db?.execSQL(createSubtasksTable)

        // Добавим тестовые данные для проверки
        addTestData(db)
    }

    private fun addTestData(db: SQLiteDatabase?) {
        // Проверяем, есть ли данные
        val cursor = db?.query(TABLE_REMINDERS, arrayOf(COLUMN_ID), null, null, null, null, null)
        val count = cursor?.count ?: 0
        cursor?.close()

        if (count == 0) {
            // Добавляем тестовую задачу, если база пуста
            val values = ContentValues().apply {
                put(COLUMN_ID, 1)
                put(COLUMN_TEXT, "Тестовая задача")
                put(COLUMN_DATE, Utils.getCurrentDate())
                put(COLUMN_TIME, "12:00")
                put(COLUMN_IS_COMPLETED, 0)
                put(COLUMN_PRIORITY, Priority.MEDIUM.name)
            }
            db?.insert(TABLE_REMINDERS, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SUBTASKS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_REMINDERS")
        onCreate(db)
    }

    // Добавим метод для отладки
    fun getAllReminders(): List<Reminder> {
        val remindersList = mutableListOf<Reminder>()
        val cursor = readableDatabase?.query(
            TABLE_REMINDERS,
            null, null, null, null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val idColumn = it.getColumnIndexOrThrow(COLUMN_ID)
                val textColumn = it.getColumnIndexOrThrow(COLUMN_TEXT)
                val dateColumn = it.getColumnIndexOrThrow(COLUMN_DATE)
                val timeColumn = it.getColumnIndexOrThrow(COLUMN_TIME)
                val completedColumn = it.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)
                val priorityColumn = it.getColumnIndexOrThrow(COLUMN_PRIORITY)

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

                    remindersList.add(Reminder(id, text, date, time, isCompleted, priority))
                } while (it.moveToNext())
            }
        }
        return remindersList
    }
}*/