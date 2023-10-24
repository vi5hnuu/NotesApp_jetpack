package com.vi5hnu.notesapp.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vi5hnu.notesapp.model.Note
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@Composable
fun NoteCard(note:Note,onDelete:(id:UUID)->Unit,onEdit:(id:UUID)->Unit) {
    Box{
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(0.1f)),
            border = BorderStroke(width = 0.5.dp, color = MaterialTheme.colorScheme.primary.copy(0.2f)),
        ) {
            Column(modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()) {
                Text(
                    text = note.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 5.dp),
                    textDecoration = TextDecoration.Underline
                )
                Text(
                    text = note.description,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(bottom = 5.dp),
                )
                Row(
                    modifier=Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {

                    Box(modifier=Modifier
                        .clip(shape = RoundedCornerShape(CornerSize(7.dp)))
                        .clickable { onEdit(note.id) }){
                        Text(
                            text = "Edit",
                            modifier=Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            textDecoration = TextDecoration.Underline)
                    }
                    Text(
                        text = SimpleDateFormat("dd MMMM yyy",Locale.ENGLISH).format(note.createdAt),
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.ExtraLight
                    )

                }
            }
        }
        IconButton(
            modifier=Modifier.align(alignment = Alignment.TopEnd),
            onClick =  {onDelete(note.id)}) {
            Icon(imageVector = Icons.Filled.Delete, contentDescription ="delete icon", tint = Color.Red.copy(0.7f) )
        }
    }
}