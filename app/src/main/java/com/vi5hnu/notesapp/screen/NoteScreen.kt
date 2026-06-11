package com.vi5hnu.notesapp.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vi5hnu.notesapp.R
import com.vi5hnu.notesapp.components.NoteCard
import com.vi5hnu.notesapp.components.NoteEditSheet
import com.vi5hnu.notesapp.model.Note
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
}

private fun formattedDate(): String =
    SimpleDateFormat("EEE, MMM d", Locale.ENGLISH).format(Date())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    darkTheme: Boolean = false,
    onThemeToggle: (Boolean) -> Unit = {}
) {
    val viewModel = viewModel<NoteViewModel>()
    val notes by viewModel.notes.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSheet by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    val displayedNotes = remember(notes, searchQuery) {
        val sorted = notes.sortedByDescending { it.createdAt }
        if (searchQuery.isBlank()) sorted
        else sorted.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { editingNote = null; showSheet = true },
                    shape = RoundedCornerShape(21.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 10.dp,
                        pressedElevation = 6.dp
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "New note",
                        modifier = Modifier.size(27.dp)
                    )
                }
            }
        },
        bottomBar = {
            TendBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> NotesTabContent(
                notes = displayedNotes,
                totalCount = notes.size,
                searchQuery = searchQuery,
                showSearch = showSearch,
                onSearchToggle = {
                    showSearch = !showSearch
                    if (!showSearch) searchQuery = ""
                },
                onSearchChange = { searchQuery = it },
                onNoteClick = { note -> editingNote = note; showSheet = true },
                modifier = Modifier.padding(innerPadding)
            )
            1 -> SettingsTabContent(
                darkTheme = darkTheme,
                onThemeToggle = onThemeToggle,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    if (showSheet) {
        NoteEditSheet(
            note = editingNote,
            onDismiss = { showSheet = false },
            onSave = { saved ->
                if (editingNote != null) viewModel.update(saved) else viewModel.addNote(saved)
                showSheet = false
            },
            onDelete = { toDelete ->
                viewModel.removeNote(toDelete.id)
                showSheet = false
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Note deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.addNote(toDelete)
                    }
                }
            }
        )
    }
}

@Composable
private fun TendBottomNav(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        )
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.note_icon),
                    contentDescription = "Notes",
                    modifier = Modifier.size(23.dp)
                )
            },
            label = {
                Text("Notes", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            },
            colors = itemColors
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(23.dp)
                )
            },
            label = {
                Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            },
            colors = itemColors
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun NotesTabContent(
    notes: List<Note>,
    totalCount: Int,
    searchQuery: String,
    showSearch: Boolean,
    onSearchToggle: () -> Unit,
    onSearchChange: (String) -> Unit,
    onNoteClick: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(showSearch) {
        if (showSearch) {
            delay(150)
            runCatching { focusRequester.requestFocus() }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ---- Header ----
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 22.dp)
                    .padding(top = 10.dp, bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wordmark
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(17.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.note_edit),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        Text(
                            "Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Search toggle button
                    Surface(
                        onClick = onSearchToggle,
                        shape = RoundedCornerShape(13.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (showSearch) Icons.Outlined.Close else Icons.Default.Search,
                                contentDescription = if (showSearch) "Close search" else "Search notes",
                                modifier = Modifier.size(21.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = !showSearch,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Column {
                        Text(
                            text = greeting(),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp,
                            lineHeight = 32.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "${formattedDate()} · $totalCount ${if (totalCount == 1) "note" else "notes"}",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                }

                AnimatedVisibility(
                    visible = showSearch,
                    enter = fadeIn() + slideInVertically { -it / 2 },
                    exit = fadeOut() + slideOutVertically { -it / 2 }
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = {
                            Text(
                                "Search notes…",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {}),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }

        // ---- Empty state ----
        if (notes.isEmpty() && !showSearch && totalCount == 0) {
            item { EmptyNotesState() }
        } else if (notes.isEmpty() && showSearch) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No results for \"$searchQuery\"",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // ---- Section header ----
            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (showSearch) "RESULTS" else "NOTES",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${notes.size}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ---- Note cards ----
            items(notes, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    onClick = { onNoteClick(note) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 10.dp)
                        .animateItemPlacement()
                )
            }
        }
    }
}

@Composable
private fun EmptyNotesState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 72.dp, bottom = 40.dp)
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.note_icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "All clear",
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Tap the + to capture your first thought — it'll be here whenever you need it.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun SettingsTabContent(
    darkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
            .padding(top = 16.dp)
    ) {
        Text(
            "Settings",
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(24.dp))

        Text(
            "APPEARANCE",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(11.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = if (darkTheme) "🌙" else "☀️",
                                fontSize = 17.sp
                            )
                        }
                    }
                    Column {
                        Text(
                            "Dark theme",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            if (darkTheme) "On" else "Off",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = darkTheme,
                    onCheckedChange = onThemeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.secondary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "DATA",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(11.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("📱", fontSize = 17.sp)
                    }
                }
                Column {
                    Text(
                        "On-device only",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Notes never leave your device",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(40.dp))

        // App version footer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(5.dp)
                        )
                )
                Text(
                    "Notes",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Version 1.0.2 · On-device",
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
