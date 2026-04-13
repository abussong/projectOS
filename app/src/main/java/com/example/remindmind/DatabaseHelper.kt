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