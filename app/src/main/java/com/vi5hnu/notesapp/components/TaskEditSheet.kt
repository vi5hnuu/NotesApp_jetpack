package com.vi5hnu.notesapp.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vi5hnu.notesapp.model.Subtask
import com.vi5hnu.notesapp.model.Task
import com.vi5hnu.notesapp.model.TaskList
import com.vi5hnu.notesapp.utils.addDays
import com.vi5hnu.notesapp.utils.dateLabel
import com.vi5hnu.notesapp.utils.nextWeekend
import com.vi5hnu.notesapp.utils.timeLabel
import com.vi5hnu.notesapp.utils.todayStr
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.UUID

private val QUICK_TIMES = listOf("07:00", "09:00", "12:00", "14:00", "18:00", "20:00")
private val RECUR_OPTIONS = listOf(null, "daily", "weekdays", "weekly", "monthly")
private val RECUR_LABELS = listOf("Once", "Daily", "Weekdays", "Weekly", "Monthly")
private val PRIORITY_OPTIONS = listOf("none", "med", "high")
private val PRIORITY_LABELS = listOf("None", "Medium", "High")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditSheet(
    task: Task?,
    lists: List<TaskList>,
    defaultListId: String = "inbox",
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isNew = task == null

    var title by remember(task) { mutableStateOf(task?.title ?: "") }
    var notes by remember(task) { mutableStateOf(task?.notes ?: "") }
    var listId by remember(task) { mutableStateOf(task?.listId ?: defaultListId) }
    var due by remember(task) { mutableStateOf(task?.due ?: todayStr()) }
    var time by remember(task) { mutableStateOf(task?.time) }
    var priority by remember(task) { mutableStateOf(task?.priority ?: "none") }
    var recur by remember(task) { mutableStateOf(task?.recur) }
    // Parse subtasks synchronously at initialisation — avoids LaunchedEffect flash/delay
    var subtasks by remember(task) {
        mutableStateOf(if (task != null) parseSubtasks(task.subtasks) else emptyList())
    }
    var showCal by remember { mutableStateOf(false) }
    var showMore by remember(task) {
        mutableStateOf(task != null && (task.notes.isNotBlank() || task.subtasks != "[]"))
    }
    var noDue by remember(task) { mutableStateOf(task?.due == null) }

    val titleFocus = remember { FocusRequester() }

    // Only auto-focus for new tasks
    LaunchedEffect(Unit) {
        if (isNew) { delay(300); runCatching { titleFocus.requestFocus() } }
    }

    val today = remember { todayStr() }
    val quickDates = remember {
        listOf(
            todayStr() to "Today",
            addDays(todayStr(), 1) to "Tomorrow",
            nextWeekend() to "Weekend",
            addDays(todayStr(), 7) to "Next week"
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                Modifier.fillMaxWidth().padding(top = 11.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    Modifier.size(38.dp, 5.dp),
                    shape = RoundedCornerShape(3.dp),
                    color = MaterialTheme.colorScheme.outline
                ) {}
            }
        }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    if (isNew) "New task" else "Edit task",
                    Modifier.weight(1f),
                    fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp, color = MaterialTheme.colorScheme.onSurface
                )
                if (!isNew) {
                    IconButton(onClick = { onDelete(task!!) }) {
                        Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                // Title
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth().focusRequester(titleFocus),
                    placeholder = {
                        Text(
                            "What needs doing?",
                            fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp, color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(Modifier.height(4.dp))

                // List chips
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    lists.forEach { l ->
                        val sel = listId == l.id
                        Surface(
                            onClick = { listId = l.id },
                            shape = RoundedCornerShape(13.dp),
                            color = if (sel) l.color.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                if (sel) 1.5.dp else 1.dp,
                                if (sel) l.color else Color.Transparent
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(
                                Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Surface(
                                    shape = CircleShape, color = l.color,
                                    modifier = Modifier.size(12.dp)
                                ) {}
                                Text(
                                    l.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                    color = if (sel) l.color else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // When
                FieldLabel("When")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickDates.forEach { (d, label) ->
                        val sel = !noDue && due == d
                        OptChip(label = label, selected = sel) {
                            due = d; noDue = false; showCal = false
                        }
                    }
                    val isCustom = !noDue && quickDates.none { it.first == due }
                    OptChip(
                        label = if (isCustom) dateLabel(due) else "Pick",
                        selected = showCal || isCustom,
                        icon = "📅"
                    ) { showCal = !showCal; noDue = false }
                    OptChip(label = "No date", selected = noDue, icon = "✕") {
                        noDue = true; time = null; showCal = false
                    }
                }

                if (showCal && !noDue) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        MiniCalendar(
                            selected = due,
                            onPick = { due = it },
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                // Reminder time (only when due is set)
                if (!noDue) {
                    Spacer(Modifier.height(4.dp))
                    FieldLabel("Reminder time")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QUICK_TIMES.forEach { t ->
                            OptChip(
                                label = timeLabel(t),
                                selected = time == t,
                                icon = "🔔"
                            ) { time = if (time == t) null else t }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Repeat
                FieldLabel("Repeat")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RECUR_OPTIONS.forEachIndexed { i, r ->
                        OptChip(
                            label = RECUR_LABELS[i],
                            selected = recur == r
                        ) { recur = r }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Priority
                FieldLabel("Priority")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PRIORITY_OPTIONS.forEachIndexed { i, p ->
                        OptChip(
                            label = PRIORITY_LABELS[i],
                            selected = priority == p,
                            accentColor = when (p) {
                                "high" -> MaterialTheme.colorScheme.primary
                                "med" -> Color(0xFFD4862E)
                                else -> null
                            }
                        ) { priority = p }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Notes & Subtasks toggle
                if (!showMore) {
                    Surface(
                        onClick = { showMore = true },
                        shape = RoundedCornerShape(13.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("+", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "Add notes & subtasks",
                                fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    FieldLabel("Notes")
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        TextField(
                            value = notes,
                            onValueChange = { notes = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Add details…",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
                                )
                            },
                            textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface),
                            maxLines = 4,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    FieldLabel("Subtasks")
                    subtasks.forEach { sub ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                onClick = {
                                    subtasks = subtasks.map {
                                        if (it.id == sub.id) it.copy(done = !it.done) else it
                                    }
                                },
                                shape = CircleShape,
                                color = if (sub.done) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                border = BorderStroke(
                                    2.dp,
                                    if (sub.done) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.size(21.dp)
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    if (sub.done) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                            TextField(
                                value = sub.title,
                                onValueChange = { v ->
                                    subtasks = subtasks.map { if (it.id == sub.id) it.copy(title = v) else it }
                                },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Subtask", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)) },
                                textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable { subtasks = subtasks.filter { it.id != sub.id } },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✕", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Surface(
                        onClick = { subtasks = subtasks + Subtask(title = "") },
                        color = Color.Transparent,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Row(
                            Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("+", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Text(
                                "Add subtask", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        val saved = if (isNew) Task(
                            title = title.trim(),
                            notes = notes.trim(),
                            listId = listId,
                            due = if (noDue) null else due,
                            time = time,
                            priority = priority,
                            recur = recur,
                            subtasks = serializeSubtasks(subtasks.filter { it.title.isNotBlank() })
                        ) else task!!.copy(
                            title = title.trim(),
                            notes = notes.trim(),
                            listId = listId,
                            due = if (noDue) null else due,
                            time = time,
                            priority = priority,
                            recur = recur,
                            subtasks = serializeSubtasks(subtasks.filter { it.title.isNotBlank() })
                        )
                        onSave(saved)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = title.trim().isNotEmpty(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(0.38f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(0.6f)
                    )
                ) {
                    Text(
                        if (isNew) "Add task" else "Save changes",
                        fontSize = 14.5.sp, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text.uppercase(),
        fontSize = 12.5.sp, fontWeight = FontWeight.Bold,
        letterSpacing = 0.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 2.dp, bottom = 8.dp, top = 4.dp)
    )
}

@Composable
private fun OptChip(
    label: String,
    selected: Boolean,
    icon: String? = null,
    accentColor: Color? = null,
    onClick: () -> Unit
) {
    val tint = accentColor ?: MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(13.dp),
        color = if (selected) tint.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            if (selected) 1.5.dp else 1.dp,
            if (selected) tint else Color.Transparent
        ),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) Text(icon, fontSize = 13.sp)
            Text(
                label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = if (selected) tint else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MiniCalendar(
    selected: String?,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = todayStr()
    var year by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }

    Column(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            Text(
                "${monthNames[month]} $year",
                fontWeight = FontWeight.Bold, fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    onClick = {
                        if (month == 0) { month = 11; year-- } else month--
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(32.dp)
                ) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("‹", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface) } }
                Surface(
                    onClick = {
                        if (month == 11) { month = 0; year++ } else month++
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(32.dp)
                ) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("›", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface) } }
            }
        }
        Spacer(Modifier.height(12.dp))
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val firstDow = Calendar.getInstance().apply { set(year, month, 1) }.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = Calendar.getInstance().apply { set(year, month, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)
        val cells = (0 until firstDow).map { null } + (1..daysInMonth).map { it }
        val weeks = cells.chunked(7)
        Row(Modifier.fillMaxWidth()) {
            listOf("S","M","T","W","T","F","S").forEach { d ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(d, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        weeks.forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { i ->
                    val day = week.getOrNull(i)
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (day != null) {
                            val dateStr = sdf.format(Calendar.getInstance().apply { set(year, month, day) }.time)
                            val isSel = dateStr == selected
                            val isToday = dateStr == today
                            Surface(
                                onClick = { onPick(dateStr) },
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    isSel -> MaterialTheme.colorScheme.primary
                                    else -> Color.Transparent
                                },
                                border = if (isToday && !isSel) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        "$day",
                                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                        color = when {
                                            isSel -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}

private fun serializeSubtasks(subtasks: List<Subtask>): String {
    val arr = org.json.JSONArray()
    subtasks.forEach { s ->
        arr.put(org.json.JSONObject().apply {
            put("id", s.id); put("title", s.title); put("done", s.done)
        })
    }
    return arr.toString()
}

private fun parseSubtasks(json: String): List<Subtask> {
    return try {
        val arr = org.json.JSONArray(json)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Subtask(id = o.getString("id"), title = o.getString("title"), done = o.getBoolean("done"))
        }
    } catch (e: Exception) { emptyList() }
}
