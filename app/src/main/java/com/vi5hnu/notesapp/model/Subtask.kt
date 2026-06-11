package com.vi5hnu.notesapp.model

import java.util.UUID

data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val done: Boolean = false
)
