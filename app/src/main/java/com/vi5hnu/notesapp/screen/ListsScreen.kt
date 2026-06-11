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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vi5hnu.notesapp.model.Task
import com.vi5hnu.notesapp.model.TaskList
import com.vi5hnu.notesapp.utils.diffDays
import com.vi5hnu.notesapp.utils.todayStr

@Composable
fun ListsScreen(
    tasks: List<Task>,
    lists: List<TaskList>,
    onPickList: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
                    "Lists", fontSize = 30.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp, color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "${lists.size} lists",
                    fontSize = 14.5.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(lists) { list ->
            val all = tasks.filter { it.listId == list.id }
            val active = all.count { !it.done }
            val done = all.count { it.done }
            val pct = if (all.isNotEmpty()) done.toFloat() / all.size else 0f
            val overdue = all.count { !it.done && it.due != null && diffDays(it.due, todayStr()) < 0 }

            Surface(
                onClick = { onPickList(list.id) },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.5f)),
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 11.dp).fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = list.color,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(listEmoji(list.id), fontSize = 22.sp)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(list.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.1).sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            buildString {
                                append("$active active")
                                if (done > 0) append(" · $done done")
                                if (overdue > 0) append(" · $overdue overdue")
                            },
                            fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (all.isNotEmpty()) {
                        ProgressRing(pct = pct, color = list.color, modifier = Modifier.size(38.dp))
                    }
                }
            }
        }

        // "New list" placeholder card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 11.dp).fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("+", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text("New list", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun listEmoji(id: String) = when (id) {
    "inbox" -> "📥"
    "work" -> "💼"
    "home" -> "🏠"
    "groceries" -> "🛒"
    "health" -> "❤️"
    else -> "📋"
}

@Composable
fun ProgressRing(pct: Float, color: Color, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = modifier) {
            val r = size.minDimension / 2f - 4.dp.toPx()
            val stroke = 4.dp.toPx()
            val trackColor = color.copy(alpha = 0.2f)
            drawCircle(color = trackColor, radius = r, style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
            if (pct > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * pct,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }
        }
        Text(
            "${(pct * 100).toInt()}",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
