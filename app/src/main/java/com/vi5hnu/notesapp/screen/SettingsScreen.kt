package com.vi5hnu.notesapp.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class AppSettings(
    val rollover: Boolean = true,
    val reminders: Boolean = true,
    val streaks: Boolean = true,
    val nudge: Boolean = true,
    val archive: Boolean = false
)

@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
            .padding(top = 16.dp)
    ) {
        Text(
            "Settings", fontSize = 30.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.5).sp, color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(24.dp))

        SettingGroupLabel("Appearance")
        SettingGroup {
            SettingRow(
                emoji = if (darkTheme) "🌙" else "☀️",
                title = "Theme",
                sub = null,
                control = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SegButton("Light", !darkTheme) { onThemeToggle(false) }
                        SegButton("Dark", darkTheme) { onThemeToggle(true) }
                    }
                }
            )
        }

        Spacer(Modifier.height(6.dp))
        SettingGroupLabel("Behavior")
        SettingGroup {
            SettingRow("⚠️", "Roll over missed tasks",
                "Overdue tasks move to “Needs attention” instead of vanishing",
                control = {
                    Switch(
                        checked = settings.rollover,
                        onCheckedChange = { onSettingsChange(settings.copy(rollover = it)) },
                        colors = switchColors()
                    )
                }
            )
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow("🔔", "Time reminders", "Notify at a task's set time",
                control = {
                    Switch(
                        checked = settings.reminders,
                        onCheckedChange = { onSettingsChange(settings.copy(reminders = it)) },
                        colors = switchColors()
                    )
                }
            )
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow("🔥", "Habit streaks", "Track streaks for repeating tasks",
                control = {
                    Switch(
                        checked = settings.streaks,
                        onCheckedChange = { onSettingsChange(settings.copy(streaks = it)) },
                        colors = switchColors()
                    )
                }
            )
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow("☀️", "Daily planning nudge", "8:00 AM — Plan your day",
                control = {
                    Switch(
                        checked = settings.nudge,
                        onCheckedChange = { onSettingsChange(settings.copy(nudge = it)) },
                        colors = switchColors()
                    )
                }
            )
        }

        Spacer(Modifier.height(6.dp))
        SettingGroupLabel("Data")
        SettingGroup {
            SettingRow("📱", "Everything stays on this device",
                "No account, no cloud — your tasks never leave your phone",
                control = { Text("📌", fontSize = 16.sp) }
            )
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow("🗂️", "Auto-archive completed", "After 30 days",
                control = {
                    Switch(
                        checked = settings.archive,
                        onCheckedChange = { onSettingsChange(settings.copy(archive = it)) },
                        colors = switchColors()
                    )
                }
            )
        }

        Spacer(Modifier.height(40.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    Modifier.size(15.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(10.dp))
                }
                Text("Tend", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(4.dp))
            Text("Version 1.0.2 · On-device", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingGroupLabel(text: String) {
    Text(
        text.uppercase(),
        fontSize = 12.5.sp, fontWeight = FontWeight.Bold,
        letterSpacing = 0.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingGroup(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.5f)),
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingRow(
    emoji: String,
    title: String,
    sub: String?,
    control: @Composable () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(11.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(36.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 17.sp)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            if (sub != null) Text(sub, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 17.sp)
        }
        control()
    }
}

@Composable
private fun SegButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9.dp),
        color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = if (selected) 1.dp else 0.dp
    ) {
        Text(
            label,
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 13.sp, fontWeight = FontWeight.Bold,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
    checkedTrackColor = MaterialTheme.colorScheme.secondary,
    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
)
