package com.vi5hnu.notesapp.widget

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import com.vi5hnu.notesapp.R
import com.vi5hnu.notesapp.model.DEFAULT_LISTS
import com.vi5hnu.notesapp.model.UserList
import com.vi5hnu.notesapp.notifications.NotificationHelper
import com.vi5hnu.notesapp.utils.diffDays
import com.vi5hnu.notesapp.utils.timeLabel
import com.vi5hnu.notesapp.utils.todayStr
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TodayWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        TodayRemoteViewsFactory(applicationContext)
}

/**
 * Builds each task row for the widget's list: a list-coloured dot, the title, and meta on the
 * right (the reminder time, or "Overdue" in the accent colour). Each row carries a fill-in intent
 * so tapping it opens that specific task.
 */
class TodayRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private data class Item(val id: String, val title: String, val dotColor: Int, val meta: String, val overdue: Boolean)

    private var items: List<Item> = emptyList()
    private val accent by lazy { ContextCompat.getColor(context, R.color.widget_accent) }
    private val subtext by lazy { ContextCompat.getColor(context, R.color.widget_subtext) }

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val deps = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val today = todayStr()
        val is24h = DateFormat.is24HourFormat(context)
        val colorMap = buildColorMap(runBlocking { deps.listRepository().getLists().first() })
        items = runBlocking { deps.taskRepository().getTasks().first() }
            // Match the app's "Today": due today, overdue, or no date — all active.
            .filter { !it.done && (it.due == null || diffDays(it.due, today) <= 0) }
            .sortedWith(compareBy({ it.due ?: "9999-99-99" }, { it.time ?: "99:99" }))
            .map { t ->
                val overdue = t.due != null && diffDays(t.due, today) < 0
                val meta = when {
                    overdue -> "Overdue"
                    t.time != null -> timeLabel(t.time, is24h)
                    else -> ""
                }
                Item(t.id.toString(), t.title, colorMap[t.listId] ?: accent, meta, overdue)
            }
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        val item = items[position]
        return RemoteViews(context.packageName, R.layout.widget_item).apply {
            setTextViewText(R.id.item_title, item.title)
            setInt(R.id.item_dot, "setColorFilter", item.dotColor)
            setTextViewText(R.id.item_meta, item.meta)
            setTextColor(R.id.item_meta, if (item.overdue) accent else subtext)
            setOnClickFillInIntent(
                R.id.item_root,
                Intent().putExtra(NotificationHelper.EXTRA_OPEN_TASK_ID, item.id)
            )
        }
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = false
    override fun onDestroy() { items = emptyList() }

    private fun buildColorMap(custom: List<UserList>): Map<String, Int> {
        val map = HashMap<String, Int>()
        DEFAULT_LISTS.forEach { map[it.id] = it.color.toArgb() }
        custom.forEach { map[it.id] = it.color.toInt() }
        return map
    }
}
