package com.vi5hnu.notesapp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vi5hnu.notesapp.R
import com.vi5hnu.notesapp.model.Note

@Composable
fun NoteForm(noteState:MutableState<Note>, isEditing:MutableState<Boolean>, onCreateOrUpdate:()->Unit) {

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = noteState.value.title,
            label = { Text(text = "Title") },
            onValueChange = { title -> noteState.value=noteState.value.copy(title=title)},
            singleLine = true,
            placeholder = { Text(text = "what i learn today ...") },
            leadingIcon = {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.note_edit),
                    contentDescription = "note title icon"
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = noteState.value.description,
            label = { Text(text = "Description") },
            onValueChange = { desc -> noteState.value=noteState.value.copy(description=desc) },
            singleLine = false,
            minLines = 2,
            maxLines = 5,
            placeholder = { Text(text = "The more that you read, the more things you will know. ...") },
            leadingIcon = {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.note_icon),
                    contentDescription = "note description icon",
                    modifier = Modifier
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Default,
                keyboardType = KeyboardType.Text
            )
        )
        Spacer(modifier = Modifier.height(7.dp))
        Row {
            ElevatedButton(
                modifier = Modifier,
                shape = RoundedCornerShape(CornerSize(5.dp)),
                enabled = noteState.value.title.trim().isNotEmpty() && noteState.value.description.trim()
                    .isNotEmpty(),
                onClick = {
                    onCreateOrUpdate();
                    noteState.value=Note();
                }) {
                Text(text = if(isEditing.value) "Update Note" else "Create Note")
            }
            Spacer(modifier = Modifier.width(7.dp))
            ElevatedButton(
                modifier = Modifier,
                shape = RoundedCornerShape(CornerSize(5.dp)),
                enabled = noteState.value.title.trim().isNotEmpty() || noteState.value.description.trim()
                    .isNotEmpty(),
                onClick = {
                    noteState.value=Note();
                }) {
                Text(text = if(isEditing.value) "Cancel Update" else "Reset")
            }
        }
    }
}