package com.vi5hnu.notesapp.db

import androidx.room.TypeConverter
import com.vi5hnu.notesapp.model.Subtask
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

class Converters {
    @TypeConverter
    fun fromDate(value: Long): Date = Date(value)

    @TypeConverter
    fun toDate(date: Date): Long = date.time

    @TypeConverter
    fun fromSubtasks(subtasks: List<Subtask>): String {
        val arr = JSONArray()
        subtasks.forEach { s ->
            arr.put(JSONObject().apply {
                put("id", s.id)
                put("title", s.title)
                put("done", s.done)
            })
        }
        return arr.toString()
    }

    @TypeConverter
    fun toSubtasks(json: String): List<Subtask> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                Subtask(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    done = obj.getBoolean("done")
                )
            }
        } catch (e: Exception) { emptyList() }
    }
}
