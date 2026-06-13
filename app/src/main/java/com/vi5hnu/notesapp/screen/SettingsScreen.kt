package com.vi5hnu.notesapp.screen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vi5hnu.notesapp.utils.showTimePicker
import com.vi5hnu.notesapp.utils.timeLabel

// Hosted legal documents and support contact (see legal.laxmi.solutions repo).
private const val PRIVACY_URL = "https://legal.laxmi.solutions/notes/privacy-policy"
private const val TERMS_URL = "https://legal.laxmi.solutions/notes/terms-of-service"
private const val SUPPORT_EMAIL = "laxmi.solutions.2025@gmail.com"

@Immutable
data class AppSettings(
    val rollover: Boolean = true,
    val reminders: Boolean = true,
    val streaks: Boolean = true,
    val nudge: Boolean = true,
    val nudgeTime: String = "08:00",
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
    val context = LocalContext.current
    val is24h = remember { android.text.format.DateFormat.is24HourFormat(context) }
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
                onClick = { onSettingsChange(settings.copy(rollover = !settings.rollover)) },
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
                onClick = { onSettingsChange(settings.copy(reminders = !settings.reminders)) },
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
                onClick = { onSettingsChange(settings.copy(streaks = !settings.streaks)) },
                control = {
                    Switch(
                        checked = settings.streaks,
                        onCheckedChange = { onSettingsChange(settings.copy(streaks = it)) },
                        colors = switchColors()
                    )
                }
            )
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingRow("☀️", "Daily planning nudge",
                "${timeLabel(settings.nudgeTime, is24h)} — Plan your day",
                onClick = { onSettingsChange(settings.copy(nudge = !settings.nudge)) },
                control = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (settings.nudge) {
                            Surface(
                                onClick = {
                                    showTimePicker(context, settings.nudgeTime) { picked ->
                                        onSettingsChange(settings.copy(nudgeTime = picked))
                                    }
                                },
                                shape = RoundedCornerShape(9.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    timeLabel(settings.nudgeTime, is24h),
                                    Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Switch(
                            checked = settings.nudge,
                            onCheckedChange = { onSettingsChange(settings.copy(nudge = it)) },
                            colors = switchColors()
                        )
                    }
                }
            )
        }

        // Honest hint: reminders/nudge are on but the OS won't show notifications.
        val notificationsBlocked = (settings.reminders || settings.nudge) &&
            !NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (notificationsBlocked) {
            NotificationsOffHint(onEnable = { openNotificationSettings(context) })
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
                onClick = { onSettingsChange(settings.copy(archive = !settings.archive)) },
                control = {
                    Switch(
                        checked = settings.archive,
                        onCheckedChange = { onSettingsChange(settings.copy(archive = it)) },
                        colors = switchColors()
                    )
                }
            )
        }

        Spacer(Modifier.height(6.dp))
        SettingGroupLabel("Legal & Support")
        SettingGroup {
            LinkRow("🔒", "Privacy Policy") { openUrl(context, PRIVACY_URL) }
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            LinkRow("📄", "Terms of Service") { openUrl(context, TERMS_URL) }
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            LinkRow("✉️", "Contact support", sub = SUPPORT_EMAIL) { sendSupportEmail(context) }
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
                Text("Notes", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(4.dp))
            Text("Version 1.0.3 · On-device", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** A tappable settings row that navigates out (e.g. opens a URL or email). */
@Composable
private fun LinkRow(emoji: String, title: String, sub: String? = null, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(11.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(36.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 17.sp) }
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            if (sub != null) Text(sub, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.Default.KeyboardArrowRight, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)
        )
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}

private fun sendSupportEmail(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SUPPORT_EMAIL"))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // No email client — fall back to copying nothing; silently ignore to avoid a crash.
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
private fun NotificationsOffHint(onEnable: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.2f)),
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🔕", fontSize = 18.sp)
            Column(Modifier.weight(1f)) {
                Text(
                    "Notifications are off", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Reminders won't appear until you turn notifications on.",
                    fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp
                )
            }
            Surface(
                onClick = onEnable,
                shape = RoundedCornerShape(100.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    "Enable", Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    runCatching { context.startActivity(intent) }
}

@Composable
private fun SettingRow(
    emoji: String,
    title: String,
    sub: String?,
    onClick: (() -> Unit)? = null,
    control: @Composable () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 15.dp),
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
