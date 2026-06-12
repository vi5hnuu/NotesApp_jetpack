package com.vi5hnu.notesapp.screen

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import com.vi5hnu.notesapp.utils.diffDays
import com.vi5hnu.notesapp.utils.todayStr
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vi5hnu.notesapp.R
import com.vi5hnu.notesapp.ads.BannerAd
import com.vi5hnu.notesapp.ads.InterstitialAdManager
import com.vi5hnu.notesapp.components.TaskEditSheet
import com.vi5hnu.notesapp.model.DEFAULT_LISTS
import com.vi5hnu.notesapp.model.Task
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    darkTheme: Boolean = false,
    onThemeToggle: (Boolean) -> Unit = {},
    adsEnabled: Boolean = false
) {
    val viewModel = viewModel<TaskViewModel>()
    val tasks by viewModel.tasks.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tend_prefs", Context.MODE_PRIVATE) }
    // Created only once ads are permitted (after consent); null until then.
    val interstitialAdManager = remember(adsEnabled) {
        if (adsEnabled) InterstitialAdManager(context) else null
    }

    var selectedTab by remember { mutableStateOf(0) }
    var reviewing by remember { mutableStateOf(false) }
    var activeListId by remember { mutableStateOf("all") }
    var showSheet by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var settings by remember {
        mutableStateOf(
            AppSettings(
                rollover = prefs.getBoolean("s_rollover", true),
                reminders = prefs.getBoolean("s_reminders", true),
                streaks = prefs.getBoolean("s_streaks", true),
                nudge = prefs.getBoolean("s_nudge", true),
                archive = prefs.getBoolean("s_archive", false)
            )
        )
    }

    val lists = DEFAULT_LISTS
    val showFab = (selectedTab == 0 || selectedTab == 1) && !reviewing
    val today = remember { todayStr() }
    val overdueCount = remember(tasks) {
        tasks.count { !it.done && it.due != null && diffDays(it.due, today) < 0 }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { editingTask = null; showSheet = true },
                    shape = RoundedCornerShape(21.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(10.dp, 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New task", modifier = Modifier.size(27.dp))
                }
            }
        },
        bottomBar = {
            Column {
                if (adsEnabled) BannerAd()
                TendNav(
                    selected = selectedTab,
                    reviewing = reviewing,
                    overdueCount = overdueCount,
                    onSelect = { tab -> selectedTab = tab; reviewing = false }
                )
            }
        }
    ) { innerPadding ->
        when {
            selectedTab == 0 && reviewing -> ReviewScreen(
                tasks = tasks, lists = lists, activeListId = activeListId,
                onToggle = { viewModel.toggle(it) },
                onReschedule = { task, date -> viewModel.reschedule(task, date) },
                onOpen = { editingTask = it; showSheet = true },
                onBack = { reviewing = false },
                modifier = Modifier.padding(innerPadding)
            )
            selectedTab == 0 -> TodayScreen(
                tasks = tasks, lists = lists, activeListId = activeListId,
                onListSelect = { activeListId = it },
                onToggle = { task ->
                    viewModel.toggle(task)
                    if (!task.done) {
                        // Trigger interstitial on every Nth task completion (only when ads are enabled)
                        (context as? Activity)?.let { act ->
                            interstitialAdManager?.onTaskCompleted(act)
                        }
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                "Task completed", "Undo", duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) viewModel.toggle(task)
                        }
                    }
                },
                onOpen = { editingTask = it; showSheet = true },
                onGoReview = { reviewing = true },
                modifier = Modifier.padding(innerPadding)
            )
            selectedTab == 1 -> ListsScreen(
                tasks = tasks, lists = lists,
                onPickList = { id -> activeListId = id; selectedTab = 0 },
                modifier = Modifier.padding(innerPadding)
            )
            selectedTab == 2 -> HistoryScreen(
                tasks = tasks, lists = lists,
                onToggle = { viewModel.toggle(it) },
                onOpen = { editingTask = it; showSheet = true },
                modifier = Modifier.padding(innerPadding)
            )
            selectedTab == 3 -> SettingsScreen(
                darkTheme = darkTheme,
                onThemeToggle = onThemeToggle,
                settings = settings,
                onSettingsChange = { s ->
                    settings = s
                    prefs.edit()
                        .putBoolean("s_rollover", s.rollover)
                        .putBoolean("s_reminders", s.reminders)
                        .putBoolean("s_streaks", s.streaks)
                        .putBoolean("s_nudge", s.nudge)
                        .putBoolean("s_archive", s.archive)
                        .apply()
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    if (showSheet) {
        TaskEditSheet(
            task = editingTask,
            lists = lists,
            defaultListId = if (activeListId == "all") "inbox" else activeListId,
            onDismiss = { showSheet = false },
            onSave = { saved ->
                if (editingTask != null) viewModel.update(saved) else viewModel.add(saved)
                showSheet = false
            },
            onDelete = { toDelete ->
                viewModel.remove(toDelete.id)
                showSheet = false
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        "Task deleted", "Undo", duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.restore(toDelete)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TendNav(selected: Int, reviewing: Boolean, overdueCount: Int, onSelect: (Int) -> Unit) {
    // Flat nav to match design: active state is color-only (terracotta), no indicator pill.
    // A tinted pill in the same hue as the icon made the selected icon blend in / disappear.
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorColor = Color.Transparent
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selected == 0 && !reviewing,
            onClick = { onSelect(0) },
            icon = {
                BadgedBox(badge = {
                    if (overdueCount > 0) Badge { Text("$overdueCount", fontSize = 9.sp) }
                }) {
                    Icon(ImageVector.vectorResource(R.drawable.note_icon), null, Modifier.size(23.dp))
                }
            },
            label = { Text("Today", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = selected == 1,
            onClick = { onSelect(1) },
            icon = { Icon(Icons.Default.List, null, Modifier.size(23.dp)) },
            label = { Text("Lists", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = selected == 2,
            onClick = { onSelect(2) },
            icon = { Icon(Icons.Default.Check, null, Modifier.size(23.dp)) },
            label = { Text("Done", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = selected == 3,
            onClick = { onSelect(3) },
            icon = { Icon(Icons.Default.Settings, null, Modifier.size(23.dp)) },
            label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = itemColors
        )
    }
}
