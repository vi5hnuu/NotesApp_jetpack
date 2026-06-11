package com.vi5hnu.notesapp.model

import androidx.compose.runtime.Immutable
import org.json.JSONArray
import java.util.UUID

@Immutable
data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val done: Boolean = false
)

fun parseSubtasks(json: String): List<Subtask> {
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            Subtask(
                id = obj.optString("id", UUID.randomUUID().toString()),
                title = obj.optString("title", ""),
                done = obj.optBoolean("done", false)
            )
        }
    } catch (e: Exception) { emptyList() }
}
