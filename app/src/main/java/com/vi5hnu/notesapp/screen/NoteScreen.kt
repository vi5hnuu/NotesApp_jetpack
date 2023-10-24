package com.vi5hnu.notesapp.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vi5hnu.notesapp.components.NoteCard
import com.vi5hnu.notesapp.components.NoteForm
import com.vi5hnu.notesapp.model.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(){
    val viewModel= viewModel<NoteViewModel>()
    val noteState= remember {
        mutableStateOf(Note(title = "", description = ""))
    }
    val isEditing= remember {
        mutableStateOf(false);
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            title = {
                Text(
                    "Notes",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
        )
    }) {innerPadding->
        Surface(modifier=Modifier.padding(innerPadding)) {
            Column(modifier=Modifier.padding(12.dp)) {
                NoteForm(noteState,isEditing){if(isEditing.value) {
                    viewModel.update(noteState.value);
                    isEditing.value=false;
                } else {
                    viewModel.addNote(noteState.value)
                }}
                Divider(modifier = Modifier.padding(horizontal = 7.dp, vertical = 12.dp))
                val notes=viewModel.notes.collectAsState().value.sortedByDescending { it.createdAt };
                if(notes.isNotEmpty()) ElevatedCard( elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.5.dp)){
                   LazyColumn(modifier= Modifier
                       .fillMaxSize()
                       .padding(7.dp)){
                        itemsIndexed(notes){
                            index,item -> NoteCard(note=item,
                            onDelete = {id->
                                viewModel.removeNote(id)
                                if(isEditing.value) noteState.value=Note()
                                       },
                            onEdit = {id->
                                val note=notes.find { note->note.id==id }!!
                                noteState.value=note.copy();
                                isEditing.value=true;
                            });
                            if(index!=notes.lastIndex) Spacer(modifier = Modifier.height(7.dp))
                        }
                    }
                }
                else Text(text = "\uD83D\uDE14 add some notes \uD83D\uDE14".uppercase(),
                    modifier= Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    textAlign = TextAlign.Center)
            }
        }
    }
}


