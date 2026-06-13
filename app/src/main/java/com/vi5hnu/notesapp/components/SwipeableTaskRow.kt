package com.vi5hnu.notesapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vi5hnu.notesapp.model.Task
import com.vi5hnu.notesapp.model.TaskList

/**
 * Wraps [TaskRow] with swipe gestures:
 * - Swipe **right** (start → end) completes the task.
 * - Swipe **left** (end → start) deletes it.
 *
 * The action runs on swipe and the row is allowed to snap back — the resulting data change
 * removes the item from the list, so there's no lingering "dismissed" state to manage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskRow(
    task: Task,
    list: TaskList?,
    today: String,
    onToggle: (Task) -> Unit,
    onClick: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    showList: Boolean = false,
    showStreak: Boolean = true,
    is24h: Boolean = false,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberDismissState(
        confirmValueChange = { value ->
            when (value) {
                DismissValue.DismissedToEnd -> onToggle(task)
                DismissValue.DismissedToStart -> onDelete(task)
                else -> {}
            }
            false // let the data change remove the row instead of retaining dismissed state
        }
    )

    SwipeToDismiss(
        state = dismissState,
        modifier = modifier,
        background = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                DismissDirection.StartToEnd -> MaterialTheme.colorScheme.secondary
                DismissDirection.EndToStart -> MaterialTheme.colorScheme.primary
                null -> Color.Transparent
            }
            val alignment =
                if (direction == DismissDirection.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            val icon = if (direction == DismissDirection.StartToEnd) Icons.Default.Check else Icons.Outlined.Delete
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 22.dp),
                contentAlignment = alignment
            ) {
                if (direction != null) Icon(icon, contentDescription = null, tint = Color.White)
            }
        },
        dismissContent = {
            TaskRow(
                task = task, list = list, today = today,
                onToggle = onToggle, onClick = onClick,
                showList = showList, showStreak = showStreak, is24h = is24h
            )
        }
    )
}
