package com.vi5hnu.notesapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = TerracottaOnPrimary,
    primaryContainer = TerracottaPrimaryContainer,
    onPrimaryContainer = TerracottaOnPrimaryContainer,
    secondary = SageSecondary,
    onSecondary = SageOnSecondary,
    background = BgLight,
    surface = SurfaceLight,
    surfaceVariant = Surface2Light,
    onBackground = InkLight,
    onSurface = InkLight,
    onSurfaceVariant = Ink2Light,
    outline = OutlineLight,
    outlineVariant = Surface3Light,
)

private val DarkColors = darkColorScheme(
    primary = TerracottaPrimary,
    onPrimary = TerracottaOnPrimary,
    primaryContainer = Color(0xFF5C2600),
    onPrimaryContainer = TerracottaPrimaryContainer,
    secondary = SageSecondary,
    onSecondary = SageOnSecondary,
    background = BgDark,
    surface = SurfaceDark,
    surfaceVariant = Surface2Dark,
    onBackground = InkDark,
    onSurface = InkDark,
    onSurfaceVariant = Ink2Dark,
    outline = OutlineDark,
    outlineVariant = Surface3Dark,
)

@Composable
fun NotesAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
