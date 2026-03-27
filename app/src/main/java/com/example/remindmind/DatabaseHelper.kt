//package com.example.remindmind
//import android.content.Context
//import android.database.sqlite.SQLiteDatabase
//import android.database.sqlite.SQLiteOpenHelper
//
//class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
//    companion object {
//        private const val DATABASE_NAME = "reminders.db"
//        private const val DATABASE_VERSION = 1
//        const val TABLE_NAME = "reminders"
//        const val COLUMN_ID = "id"
//        const val COLUMN_TEXT = "text"
//        const val COLUMN_DATE = "date"
//        const val COLUMN_TIME = "time"
//    }
//
//    override fun onCreate(db: SQLiteDatabase?) {
//        val createTableQuery = "CREATE TABLE $TABLE_NAME($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_TEXT TEXT, $COLUMN_DATE TEXT, $COLUMN_TIME TEXT)"
//        db?.execSQL(createTableQuery)
//    }
//
//    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
//        db?.execSQL(dropTableQuery)
//        onCreate(db)
//    }
//}
package com.example.remindmind

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "reminders.db"
        private const val DATABASE_VERSION = 4  // Увеличиваем версию

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
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TEXT TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_TIME TEXT,
                $COLUMN_IS_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_PRIORITY TEXT DEFAULT 'MEDIUM'
            )
        """.trimIndent()

        val createSubtasksTable = """
            CREATE TABLE $TABLE_SUBTASKS(
                $COLUMN_SUBTASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
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
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SUBTASKS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_REMINDERS")
        onCreate(db)
    }
}