package com.example.remindmind

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}

data class SubTask(
    val id: Int,
    val text: String,
    val date: String,
    val time: String,
    var isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM
)

data class Reminder(
    val id: Int,
    val text: String,
    val date: String,
    val time: String,
    var isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val subTasks: SnapshotStateList<SubTask> = mutableStateListOf()  // Изменяем тип
)