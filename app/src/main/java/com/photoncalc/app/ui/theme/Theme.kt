package com.photoncalc.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF005FAE),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD4E3FF),
    onPrimaryContainer = Color(0xFF001C39),
    secondary = Color(0xFF00696E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF9CF1F5),
    onSecondaryContainer = Color(0xFF002022),
    tertiary = Color(0xFFB4581F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDBC8),
    onTertiaryContainer = Color(0xFF351000),
    background = Color(0xFFF8F9FC),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFF8F9FC),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFF73777F),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA3C9FF),
    onPrimary = Color(0xFF00315D),
    primaryContainer = Color(0xFF004783),
    onPrimaryContainer = Color(0xFFD4E3FF),
    secondary = Color(0xFF80D4D9),
    onSecondary = Color(0xFF003739),
    secondaryContainer = Color(0xFF004F53),
    onSecondaryContainer = Color(0xFF9CF1F5),
    tertiary = Color(0xFFFFB784),
    onTertiary = Color(0xFF5B2000),
    tertiaryContainer = Color(0xFF834000),
    onTertiaryContainer = Color(0xFFFFDBC8),
    background = Color(0xFF111418),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF111418),
    onSurface = Color(0xFFE1E2E8),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C6CF),
    outline = Color(0xFF8D9199),
)

@Composable
fun PhotonCalcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
