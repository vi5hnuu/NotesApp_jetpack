package com.vi5hnu.notesapp.model

import java.time.Instant
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

data class Note(
    val id:UUID= UUID.randomUUID(),
    val title:String,
    val description:String,
    val createdAt:Date= Date.from(Instant.now())
    )