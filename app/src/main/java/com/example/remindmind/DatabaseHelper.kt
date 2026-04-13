package com.example.remindmind

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.provider.Settings

/**
 * Помощник для работы с базой данных SQLite.
 *
 * Управляет созданием и обновлением базы данных для хранения напоминаний и подзадач.
 * Версия базы данных 6 добавлена для поддержки настроек уведомлений.
 *
 * @property context Контекст приложения
 *
 * @author Грехов М.В., Яньшина А.Ю.
 * @since 1.0.0
 * @version 2.0.0
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        /** Имя файла базы данных */
        private const val DATABASE_NAME = "reminders.db"

        /** Версия базы данных (6 - с поддержкой кастомизации уведомлений) */
        private const val DATABASE_VERSION = 6

        /** Название таблицы напоминаний */
        const val TABLE_REMINDERS = "reminders"

        /** ID напоминания (первичный ключ) */
        const val COLUMN_ID = "id"

        /** Текст напоминания */
        const val COLUMN_TEXT = "text"

        /** Дата напоминания (формат dd.MM.yyyy) */
        const val COLUMN_DATE = "date"

        /** Время напоминания (формат HH:mm) */
        const val COLUMN_TIME = "time"

        /** Статус выполнения (1 - выполнено, 0 - не выполнено) */
        const val COLUMN_IS_COMPLETED = "is_completed"

        /** Приоритет задачи (HIGH, MEDIUM, LOW) */
        const val COLUMN_PRIORITY = "priority"

        // Новые колонки для кастомизации уведомлений (добавлены в версии 2.0.0)

        /** Тип повторения (ONCE, DAILY, WEEKLY) */
        const val COLUMN_REPEAT_TYPE = "repeat_type"

        /** Выбранные дни для еженедельного повтора (через запятую) */
        const val COLUMN_SELECTED_DAYS = "selected_days"

        /** URI звука уведомления */
        const val COLUMN_SOUND_URI = "sound_uri"

        /** Включена ли вибрация (1 - да, 0 - нет) */
        const val COLUMN_VIBRATION_ENABLED = "vibration_enabled"

        /** Количество повторов уведомления */
        const val COLUMN_REPEAT_COUNT = "repeat_count"

        /** Интервал между повторами в минутах */
        const val COLUMN_REPEAT_INTERVAL = "repeat_interval"

        /** Название таблицы подзадач */
        const val TABLE_SUBTASKS = "subtasks"

        /** ID подзадачи (первичный ключ) */
        const val COLUMN_SUBTASK_ID = "subtask_id"

        /** ID родительского напоминания (внешний ключ) */
        const val COLUMN_PARENT_ID = "parent_id"

        /** Текст подзадачи */
        const val COLUMN_SUBTASK_TEXT = "subtask_text"

        /** Дата подзадачи */
        const val COLUMN_SUBTASK_DATE = "subtask_date"

        /** Время подзадачи */
        const val COLUMN_SUBTASK_TIME = "subtask_time"

        /** Статус выполнения подзадачи */
        const val COLUMN_SUBTASK_IS_COMPLETED = "subtask_is_completed"

        /** Приоритет подзадачи */
        const val COLUMN_SUBTASK_PRIORITY = "subtask_priority"

        // Новые колонки для кастомизации уведомлений подзадач (добавлены в версии 2.0.0)

        /** Тип повторения для подзадачи */
        const val COLUMN_SUBTASK_REPEAT_TYPE = "subtask_repeat_type"

        /** Выбранные дни для подзадачи */
        const val COLUMN_SUBTASK_SELECTED_DAYS = "subtask_selected_days"

        /** URI звука для подзадачи */
        const val COLUMN_SUBTASK_SOUND_URI = "subtask_sound_uri"

        /** Вибрация для подзадачи */
        const val COLUMN_SUBTASK_VIBRATION_ENABLED = "subtask_vibration_enabled"

        /** Количество повторов для подзадачи */
        const val COLUMN_SUBTASK_REPEAT_COUNT = "subtask_repeat_count"

        /** Интервал повторов для подзадачи */
        const val COLUMN_SUBTASK_REPEAT_INTERVAL = "subtask_repeat_interval"
    }

    /**
     * Создает таблицы базы данных при первом запуске.
     *
     * @param db База данных SQLite
     *
     * @author Грехов М.В., Яньшина А.Ю.
     * @since 1.0.0
     */
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

    /**
     * Обновляет базу данных при изменении версии.
     * Удаляет старые таблицы и создает новые.
     *
     * @param db База данных SQLite
     * @param oldVersion Старая версия
     * @param newVersion Новая версия
     *
     * @author Грехов М.В., Яньшина А.Ю.
     * @since 1.0.0
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SUBTASKS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_REMINDERS")
        onCreate(db)
    }
}