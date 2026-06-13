package com.vi5hnu.notesapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * A user-created list, persisted in Room. Built-in lists ([DEFAULT_LISTS]) are not stored here —
 * they are merged with these at read time. [color] is a packed ARGB value.
 */
@Entity(tableName = "user_lists")
data class UserList(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Long
)
