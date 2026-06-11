package com.vi5hnu.notesapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val notes: String = "",
    @ColumnInfo(name = "list_id") val listId: String = "inbox",
    val due: String? = null,                    // YYYY-MM-DD
    val time: String? = null,                   // HH:mm
    val priority: String = "none",              // "none" | "med" | "high"
    val done: Boolean = false,
    @ColumnInfo(name = "completed_at") val completedAt: String? = null,
    val recur: String? = null,                  // "daily"|"weekdays"|"weekly"|"monthly"
    val streak: Int = 0,
    val subtasks: String = "[]",                // JSON array of Subtask
    @ColumnInfo(name = "created_at") val createdAt: Date = Date()
)
