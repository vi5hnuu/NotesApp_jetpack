package com.vi5hnu.notesapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id:UUID= UUID.randomUUID(),
    @ColumnInfo(name = "title") var title:String,
    @ColumnInfo(name = "description") var description:String,
    @ColumnInfo(name = "created_at") val createdAt:Date= Date.from(Instant.now())
    ){
    constructor() : this(title="",description="") }