package com.vi5hnu.notesapp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
fun NoteForm(onCreate:(note:Note)->Unit) {
    val titleState= remember {
        mutableStateOf("")
    }
    val descriptionState= remember {
        mutableStateOf("")
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = titleState.value,
            label = { Text(text = "Title") },
            onValueChange = { title -> titleState.value = title },
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
            value = descriptionState.value,
            label = { Text(text = "Description") },
            onValueChange = { desc -> descriptionState.value = desc },
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
        ElevatedButton(
            modifier = Modifier,
            shape = RoundedCornerShape(CornerSize(5.dp)),
            enabled = titleState.value.trim().isNotEmpty() && descriptionState.value.trim()
                .isNotEmpty(),
            onClick = {
                onCreate(Note(title = titleState.value, description = descriptionState.value));
                titleState.value="";
                descriptionState.value="";
            }) {
            Text(text = "Create Note")
        }
    }
}