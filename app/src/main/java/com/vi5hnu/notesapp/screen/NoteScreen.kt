package com.vi5hnu.notesapp.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vi5hnu.notesapp.R
import com.vi5hnu.notesapp.components.NoteCard
import com.vi5hnu.notesapp.components.NoteForm
import com.vi5hnu.notesapp.data.DummyData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(navController: NavController){
    val viewModel=NoteViewModel()

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
            actions = {
                IconButton(onClick = { /* do something */ }) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "notification icon",
                        tint = Color.White,
                    )
                }
            },
            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
        )
    }) {innerPadding->
        Surface(modifier=Modifier.padding(innerPadding)) {
            Column(modifier=Modifier.padding(12.dp)) {
                NoteForm{note->viewModel.addNote(note)}
                Divider(modifier = Modifier.padding(horizontal = 7.dp, vertical = 12.dp))
                val notes=viewModel.getNotes();
                if(notes.isNotEmpty()) ElevatedCard( elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.5.dp)){
                   LazyColumn(modifier= Modifier
                        .fillMaxSize()
                        .padding(7.dp)){
                        itemsIndexed(notes){
                            index,item -> NoteCard(note=item){id->viewModel.removeNote(id)};
                            if(index!=notes.lastIndex) Spacer(modifier = Modifier.height(7.dp))
                        }
                    }
                }
                else Text(text = "\uD83D\uDE14 add some notes \uD83D\uDE14".uppercase(),
                    modifier=Modifier.fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    textAlign = TextAlign.Center)
            }
        }
    }
}


