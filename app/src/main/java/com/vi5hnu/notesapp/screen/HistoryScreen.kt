package com.vi5hnu.notesapp.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vi5hnu.notesapp.components.TaskRow
import com.vi5hnu.notesapp.model.Task
import com.vi5hnu.notesapp.model.TaskList
import com.vi5hnu.notesapp.utils.dayHeadLabel
import com.vi5hnu.notesapp.utils.diffDays
import com.vi5hnu.notesapp.utils.todayStr

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    tasks: List<Task>,
    lists: List<TaskList>,
    onToggle: (Task) -> Unit,
    onOpen: (Task) -> Unit,
    showStreak: Boolean = true,
    autoArchive: Boolean = false,
    is24h: Boolean = false,
    modifier: Modifier = Modifier
) {
    val today = remember { todayStr() }
    val listMap = remember(lists) { lists.associateBy { it.id } }
    // Memoized — avoids repeated sort/group on every recomposition (e.g. scroll).
    // When auto-archive is on, completions older than 30 days are hidden from history.
    val done = remember(tasks, autoArchive, today) {
        tasks.asSequence()
            .filter { it.done && it.completedAt != null }
            .filter { !autoArchive || diffDays(it.completedAt!!, today) >= -30 }
            .sortedByDescending { it.completedAt }
            .toList()
    }
    val totalDone = done.size
    val grouped = remember(done) {
        done.groupBy { it.completedAt!! }.entries.sortedByDescending { it.key }
    }
    val topStreak = remember(tasks, showStreak) {
        if (!showStreak) null
        else tasks.filter { it.recur != null && it.streak > 1 }.maxByOrNull { it.streak }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Column(
                Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = 22.dp).padding(top = 10.dp, bottom = 16.dp)
            ) {
                Text(
                    "Done", fontSize = 30.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp, color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "$totalDone completed",
                    fontSize = 14.5.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Streak hero
        if (topStreak != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 14.dp).fillMaxWidth()
                ) {
                    androidx.compose.foundation.layout.Row(
                        Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Filled.LocalFireDepartment, null, Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(
                                "${topStreak.streak} days",
                                fontSize = 40.sp, fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp, lineHeight = 42.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Longest streak — \"${topStreak.title}\"",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (grouped.isEmpty()) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(top = 60.dp, bottom = 40.dp).padding(horizontal = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(Modifier.size(96.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.TaskAlt, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("No history yet", fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(6.dp))
                    Text("Completed tasks gather here, grouped by day.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        } else {
            grouped.forEach { (day, dayTasks) ->
                stickyHeader(key = "head_$day") {
                    DayHead(
                        label = dayHeadLabel(day),
                        count = dayTasks.size,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 24.dp)
                            .padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(dayTasks, key = { it.id }) { task ->
                    TaskRow(
                        task = task, list = listMap[task.listId], today = today,
                        onToggle = onToggle, onClick = onOpen,
                        showList = true,
                        showStreak = showStreak,
                        is24h = is24h,
                        modifier = Modifier
                            .animateItemPlacement()
                            .padding(horizontal = 16.dp).padding(bottom = 10.dp)
                    )
                }
            }
        }
    }
}
