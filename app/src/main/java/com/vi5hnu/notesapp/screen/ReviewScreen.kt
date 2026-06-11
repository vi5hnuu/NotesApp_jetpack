package com.vi5hnu.notesapp.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vi5hnu.notesapp.model.Task
import com.vi5hnu.notesapp.model.TaskList
import com.vi5hnu.notesapp.utils.addDays
import com.vi5hnu.notesapp.utils.diffDays
import com.vi5hnu.notesapp.utils.todayStr

@Composable
fun ReviewScreen(
    tasks: List<Task>,
    lists: List<TaskList>,
    activeListId: String,
    onToggle: (Task) -> Unit,
    onReschedule: (Task, String) -> Unit,
    onOpen: (Task) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { todayStr() }
    val inList: (Task) -> Boolean = { activeListId == "all" || it.listId == activeListId }
    val overdue = tasks.filter { inList(it) && !it.done && it.due != null && diffDays(it.due, today) < 0 }
        .sortedBy { it.due }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Column(
                Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = 22.dp).padding(top = 10.dp, bottom = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = onBack,
                        shape = RoundedCornerShape(13.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ArrowBack, null,
                                modifier = Modifier.size(21.dp), tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Text(
                        "Review", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Needs attention", fontSize = 30.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp, lineHeight = 32.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "These slipped past their day. Reschedule or check them off — nothing disappears on its own.",
                    fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        if (overdue.isEmpty()) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(top = 60.dp, bottom = 40.dp).padding(horizontal = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(Modifier.size(96.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.secondary.copy(0.15f)) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(44.dp))
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("You're caught up", fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(6.dp))
                    Text("No overdue tasks. Nicely tended.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Back to today", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(overdue, key = { it.id }) { task ->
                val od = -diffDays(task.due!!, today)
                val list = lists.find { it.id == task.listId }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.5f)),
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 10.dp).fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(13.dp)
                        ) {
                            Surface(
                                onClick = { onToggle(task) },
                                shape = CircleShape,
                                color = Color.Transparent,
                                border = BorderStroke(2.2.dp, MaterialTheme.colorScheme.primary),
                                modifier = Modifier.size(24.dp)
                            ) {}
                            Column(Modifier.weight(1f)) {
                                Text(
                                    task.title, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(shape = RoundedCornerShape(7.dp), color = MaterialTheme.colorScheme.primary.copy(0.12f)) {
                                        Text(
                                            "⚠ ${if (od == 1) "1 day overdue" else "$od days overdue"}",
                                            Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (list != null) {
                                        Surface(shape = RoundedCornerShape(7.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                            Row(
                                                Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Surface(shape = CircleShape, color = list.color, modifier = Modifier.size(7.dp)) {}
                                                Text(list.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Today" to today,
                                "Tomorrow" to addDays(today, 1),
                                "Next week" to addDays(today, 7)
                            ).forEach { (label, date) ->
                                Surface(
                                    onClick = { onReschedule(task, date) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (label == "Today") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.5f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        label, Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                                        fontSize = 12.5.sp, fontWeight = FontWeight.Bold,
                                        color = if (label == "Today") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            Surface(
                                onClick = { onOpen(task) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.5f))
                            ) {
                                Text(
                                    "📅", Modifier.padding(10.dp), fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
