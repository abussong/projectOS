package com.example.remindmind

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf

enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}

data class SubTask(
    val id: Int,
    var text: String,
    val date: String,
    val time: String,
    var isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val notificationSettings: NotificationSettings = NotificationSettings() // Добавляем настройки
)

data class Reminder(
    val id: Int,
    var text: String,
    val date: String,
    val time: String,
    var isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val subTasks: SnapshotStateList<SubTask> = mutableStateListOf(),
    val notificationSettings: NotificationSettings = NotificationSettings() // Добавляем настройки
)