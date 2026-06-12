package com.vi5hnu.notesapp.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vi5hnu.notesapp.components.TaskRow
import com.vi5hnu.notesapp.model.Task
import com.vi5hnu.notesapp.model.TaskList
import com.vi5hnu.notesapp.utils.dateLabel
import com.vi5hnu.notesapp.utils.diffDays
import com.vi5hnu.notesapp.utils.greeting
import com.vi5hnu.notesapp.utils.longDate
import com.vi5hnu.notesapp.utils.todayStr

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun TodayScreen(
    tasks: List<Task>,
    lists: List<TaskList>,
    activeListId: String,
    onListSelect: (String) -> Unit,
    onToggle: (Task) -> Unit,
    onOpen: (Task) -> Unit,
    onGoReview: () -> Unit,
    showStreak: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Computed once per composition session — date string won't change mid-session
    val today = remember { todayStr() }
    val greetingText = remember { greeting() }
    val dateText = remember { longDate() }
    // Stable id -> list lookup so each row gets an @Immutable TaskList instead of scanning the list
    val listMap = remember(lists) { lists.associateBy { it.id } }

    var searchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    // All derived lists memoized — avoids repeated O(n log n) work on every recomposition
    val pool = remember(tasks, searchQuery, activeListId) {
        tasks.filter { task ->
            (activeListId == "all" || task.listId == activeListId) &&
            (searchQuery.isBlank() ||
                task.title.contains(searchQuery, ignoreCase = true) ||
                task.notes.contains(searchQuery, ignoreCase = true))
        }
    }
    val overdue = remember(pool) {
        pool.filter { !it.done && it.due != null && diffDays(it.due, today) < 0 }.sortedBy { it.due }
    }
    val todayTasks = remember(pool) {
        pool.filter { !it.done && it.due == today }.sortedBy { it.time ?: "99:99" }
    }
    val noDate = remember(pool) { pool.filter { !it.done && it.due == null } }
    val upcoming = remember(pool) {
        pool.filter { !it.done && it.due != null && diffDays(it.due, today) > 0 }
            .sortedWith(compareBy({ it.due }, { it.time ?: "99:99" }))
    }
    val completedToday = remember(pool) { pool.filter { it.done && it.completedAt == today } }
    val upcomingGroups = remember(upcoming) {
        upcoming.groupBy { it.due!! }.entries.sortedBy { it.key }
    }
    val dueTodayCount = remember(todayTasks, overdue) { todayTasks.size + overdue.size }
    val totalActive = remember(tasks) { tasks.count { !it.done } }
    // Memoize concatenation to avoid a new list allocation on every recomposition
    val todayAndNoDate = remember(todayTasks, noDate) { todayTasks + noDate }

    var showDone by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ---- Header ----
        item {
            Column(
                Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = 22.dp).padding(top = 10.dp, bottom = 4.dp)
            ) {
                if (searchActive) {
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(13.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Search, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                Box(Modifier.weight(1f)) {
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                        textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface),
                                        singleLine = true
                                    )
                                    if (searchQuery.isEmpty()) {
                                        Text("Search tasks...", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (searchQuery.isNotEmpty()) {
                                    Surface(
                                        onClick = { searchQuery = "" },
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Close, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                        Text(
                            "Cancel",
                            Modifier.clickable { keyboard?.hide(); searchActive = false; searchQuery = "" },
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                } else {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Box(
                                Modifier.size(17.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(11.dp))
                            }
                            Text(
                                "Notes", fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.2).sp, color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Surface(
                            onClick = { searchActive = true },
                            shape = RoundedCornerShape(13.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Search, null, modifier = Modifier.size(21.dp), tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        greetingText, fontSize = 30.sp, fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp, lineHeight = 32.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "$dateText · $dueTodayCount due today",
                        fontSize = 14.5.sp, fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // ---- List chips ----
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ListChip(
                        label = "All", count = totalActive,
                        selected = activeListId == "all", dotColor = null
                    ) { onListSelect("all") }
                }
                items(lists) { l ->
                    val cnt = tasks.count { !it.done && it.listId == l.id }
                    ListChip(
                        label = l.name, count = cnt,
                        selected = activeListId == l.id, dotColor = l.color
                    ) { onListSelect(l.id) }
                }
            }
        }

        // ---- Needs attention ----
        if (overdue.isNotEmpty()) {
            item {
                AttentionCard(
                    count = overdue.size,
                    onReview = onGoReview,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // ---- Empty state ----
        if (todayTasks.isEmpty() && noDate.isEmpty() && upcoming.isEmpty() && completedToday.isEmpty() && overdue.isEmpty()) {
            item { EmptyTodayState() }
        }

        // ---- Today section ----
        if (todayAndNoDate.isNotEmpty()) {
            item {
                SectionHead(label = "Today", count = todayAndNoDate.size)
            }
            items(todayAndNoDate, key = { it.id }) { task ->
                TaskRow(
                    task = task, list = listMap[task.listId], today = today,
                    onToggle = onToggle, onClick = onOpen,
                    showList = activeListId == "all",
                    showStreak = showStreak,
                    modifier = Modifier
                        .animateItemPlacement()
                        .padding(horizontal = 16.dp).padding(bottom = 10.dp)
                )
            }
        }

        // ---- Upcoming sections ----
        if (upcomingGroups.isNotEmpty()) {
            item { SectionHead(label = "Upcoming", count = null) }
            upcomingGroups.forEach { (day, dayTasks) ->
                item(key = "head_$day") {
                    DayHead(
                        label = dateLabel(day),
                        count = dayTasks.size,
                        suffix = "",
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
                items(dayTasks, key = { it.id }) { task ->
                    TaskRow(
                        task = task, list = listMap[task.listId], today = today,
                        onToggle = onToggle, onClick = onOpen,
                        showList = activeListId == "all",
                        showStreak = showStreak,
                        modifier = Modifier
                            .animateItemPlacement()
                            .padding(horizontal = 16.dp).padding(bottom = 10.dp)
                    )
                }
            }
        }

        // ---- Completed today ----
        if (completedToday.isNotEmpty()) {
            item {
                Surface(
                    onClick = { showDone = !showDone },
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "COMPLETED TODAY", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CountBadge(completedToday.size)
                        Spacer(Modifier.weight(1f))
                        Text(
                            if (showDone) "▲" else "▼",
                            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (showDone) {
                items(completedToday, key = { it.id }) { task ->
                    TaskRow(
                        task = task, list = listMap[task.listId], today = today,
                        onToggle = onToggle, onClick = onOpen,
                        showList = activeListId == "all",
                        showStreak = showStreak,
                        modifier = Modifier
                            .animateItemPlacement()
                            .padding(horizontal = 16.dp).padding(bottom = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AttentionCard(count: Int, onReview: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.2f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(11.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(38.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("⚠", fontSize = 18.sp)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "Needs attention",
                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp, color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "$count task${if (count > 1) "s" else ""} rolled over from earlier",
                    fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                onClick = onReview,
                shape = RoundedCornerShape(100.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    "Review",
                    Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun ListChip(
    label: String,
    count: Int,
    selected: Boolean,
    dotColor: androidx.compose.ui.graphics.Color?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            if (dotColor != null) {
                Surface(shape = CircleShape, color = dotColor, modifier = Modifier.size(9.dp)) {}
            }
            Text(
                label, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold,
                color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (count > 0) {
                Text(
                    "$count", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = if (selected) MaterialTheme.colorScheme.background.copy(0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                )
            }
        }
    }
}

@Composable
private fun SectionHead(label: String, count: Int?) {
    Row(
        Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            label.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (count != null) CountBadge(count)
    }
}

@Composable
private fun CountBadge(count: Int) {
    Surface(shape = RoundedCornerShape(100.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(
            "$count", Modifier.padding(horizontal = 8.dp, vertical = 1.dp),
            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DayHead(label: String, count: Int, suffix: String = "done", modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.width(10.dp))
        Surface(Modifier.weight(1f).height(1.dp), color = MaterialTheme.colorScheme.outline.copy(0.5f)) {}
        Spacer(Modifier.width(10.dp))
        Text(
            if (suffix.isBlank()) "$count" else "$count $suffix",
            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyTodayState() {
    Column(
        Modifier.fillMaxWidth().padding(top = 72.dp, bottom = 40.dp).padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(Modifier.size(96.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("🌿", fontSize = 40.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("All clear", fontSize = 19.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(6.dp))
        Text(
            "Nothing due today. Tap the + to plant your next task — it'll be waiting when you need it.",
            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

