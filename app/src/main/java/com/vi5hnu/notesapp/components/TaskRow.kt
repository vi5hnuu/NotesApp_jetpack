package com.vi5hnu.notesapp.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vi5hnu.notesapp.model.Task
import com.vi5hnu.notesapp.model.TaskList
import com.vi5hnu.notesapp.utils.dateLabel
import com.vi5hnu.notesapp.utils.diffDays
import com.vi5hnu.notesapp.utils.timeLabel
import com.vi5hnu.notesapp.utils.todayStr

@Composable
fun TaskRow(
    task: Task,
    lists: List<TaskList>,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    showList: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isOverdue = !task.done && task.due != null && diffDays(task.due, todayStr()) < 0
    val list = lists.find { it.id == task.listId }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = if (task.priority == "high" && !task.done)
            BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
        else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp)
                .alpha(if (task.done) 0.55f else 1f),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            // Checkbox
            val checkBg by animateColorAsState(
                if (task.done) MaterialTheme.colorScheme.secondary
                else Color.Transparent,
                label = "checkBg"
            )
            val checkBorder by animateColorAsState(
                when {
                    task.done -> MaterialTheme.colorScheme.secondary
                    task.priority == "high" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                },
                label = "checkBorder"
            )
            Surface(
                onClick = onToggle,
                shape = CircleShape,
                color = checkBg,
                border = BorderStroke(2.2.dp, checkBorder),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
                    if (task.done) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.1).sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.notes.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = task.notes,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!task.done) {
                    Spacer(Modifier.height(7.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Due date badge
                        if (task.due != null) {
                            MetaBadge(
                                text = buildString {
                                    append(dateLabel(task.due))
                                    if (task.time != null) append(" · ${timeLabel(task.time)}")
                                },
                                warn = isOverdue
                            )
                        }
                        // Streak badge
                        if (task.recur != null && task.streak > 1) {
                            MetaBadge(text = "🔥 ${task.streak}", warn = false)
                        }
                        // List tag
                        if (showList && list != null) {
                            Row(
                                modifier = Modifier
                                    .height(22.dp)
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = list.color,
                                    modifier = Modifier.size(7.dp)
                                ) {}
                                Text(
                                    text = list.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaBadge(text: String, warn: Boolean) {
    Surface(
        shape = RoundedCornerShape(7.dp),
        color = if (warn) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (warn) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
