package com.vi5hnu.notesapp.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vi5hnu.notesapp.model.Note
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditSheet(
    note: Note?,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit,
    onDelete: (Note) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember(note) { mutableStateOf(note?.title ?: "") }
    var description by remember(note) { mutableStateOf(note?.description ?: "") }
    val titleFocus = remember { FocusRequester() }
    val isNew = note == null
    val canSave = title.trim().isNotEmpty() && description.trim().isNotEmpty()

    LaunchedEffect(Unit) {
        if (isNew) {
            delay(300)
            runCatching { titleFocus.requestFocus() }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 11.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(38.dp, 5.dp),
                    shape = RoundedCornerShape(3.dp),
                    color = MaterialTheme.colorScheme.outline
                ) {}
            }
        }
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (isNew) "New note" else "Edit note",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (!isNew) {
                IconButton(onClick = { onDelete(note!!) }) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete note",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .imePadding()
        ) {
            // Large title input (borderless)
            TextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocus),
                placeholder = {
                    Text(
                        "Title",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    )
                },
                textStyle = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp,
                    color = MaterialTheme.colorScheme.onSurface
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

            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Description input (surface-variant background)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp),
                    placeholder = {
                        Text(
                            "Add details…",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    maxLines = 8,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(Modifier.height(18.dp))

            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val saved = if (isNew) {
                        Note(title = title.trim(), description = description.trim())
                    } else {
                        note!!.copy(title = title.trim(), description = description.trim())
                    }
                    onSave(saved)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = canSave,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                )
            ) {
                Text(
                    text = if (isNew) "Add note" else "Save changes",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}
