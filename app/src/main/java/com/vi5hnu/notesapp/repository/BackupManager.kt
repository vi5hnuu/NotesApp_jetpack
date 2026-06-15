package com.vi5hnu.notesapp.repository

import android.content.Context
import android.net.Uri
import com.vi5hnu.notesapp.model.Task
import com.vi5hnu.notesapp.model.UserList
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Parsed contents of a backup file. */
data class BackupData(val tasks: List<Task>, val lists: List<UserList>)

/**
 * Serializes tasks + custom lists to a JSON backup and reads them back, via Storage Access
 * Framework URIs. All file IO is failure-safe (returns false/null instead of throwing).
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun exportJson(tasks: List<Task>, lists: List<UserList>): String {
        val root = JSONObject()
        root.put("version", BACKUP_VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        root.put("tasks", JSONArray().apply {
            tasks.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id.toString())
                    put("title", t.title)
                    put("notes", t.notes)
                    put("listId", t.listId)
                    put("due", t.due ?: JSONObject.NULL)
                    put("time", t.time ?: JSONObject.NULL)
                    put("priority", t.priority)
                    put("done", t.done)
                    put("completedAt", t.completedAt ?: JSONObject.NULL)
                    put("recur", t.recur ?: JSONObject.NULL)
                    put("until", t.until ?: JSONObject.NULL)
                    put("streak", t.streak)
                    put("subtasks", t.subtasks)
                    put("createdAt", t.createdAt.time)
                })
            }
        })

        root.put("lists", JSONArray().apply {
            lists.forEach { l ->
                put(JSONObject().apply {
                    put("id", l.id); put("name", l.name); put("color", l.color)
                })
            }
        })
        return root.toString()
    }

    fun writeToUri(uri: Uri, json: String): Boolean = runCatching {
        context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
        true
    }.getOrDefault(false)

    fun readFromUri(uri: Uri): String? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
    }.getOrNull()

    fun parse(json: String): BackupData? = runCatching {
        val root = JSONObject(json)
        val tasks = root.getJSONArray("tasks").let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Task(
                    id = UUID.fromString(o.getString("id")),
                    title = o.getString("title"),
                    notes = o.optString("notes", ""),
                    listId = o.optString("listId", "inbox"),
                    due = o.stringOrNull("due"),
                    time = o.stringOrNull("time"),
                    priority = o.optString("priority", "none"),
                    done = o.optBoolean("done", false),
                    completedAt = o.stringOrNull("completedAt"),
                    recur = o.stringOrNull("recur"),
                    until = o.stringOrNull("until"),
                    streak = o.optInt("streak", 0),
                    subtasks = o.optString("subtasks", "[]"),
                    createdAt = Date(o.optLong("createdAt", System.currentTimeMillis()))
                )
            }
        }
        val lists = root.optJSONArray("lists")?.let { arr ->
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                UserList(id = o.getString("id"), name = o.getString("name"), color = o.getLong("color"))
            }
        } ?: emptyList()
        BackupData(tasks, lists)
    }.getOrNull()

    private fun JSONObject.stringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key, null)

    private companion object {
        const val BACKUP_VERSION = 1
    }
}
