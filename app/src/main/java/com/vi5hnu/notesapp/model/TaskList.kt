package com.vi5hnu.notesapp.model

import androidx.compose.ui.graphics.Color

data class TaskList(
    val id: String,
    val name: String,
    val color: Color
)

val DEFAULT_LISTS = listOf(
    TaskList("inbox",     "Inbox",     Color(0xFFDF5F3A)),
    TaskList("work",      "Work",      Color(0xFF2B5FD1)),
    TaskList("home",      "Home",      Color(0xFF2F6F4E)),
    TaskList("groceries", "Groceries", Color(0xFFD4862E)),
    TaskList("health",    "Health",    Color(0xFFD4486B))
)
