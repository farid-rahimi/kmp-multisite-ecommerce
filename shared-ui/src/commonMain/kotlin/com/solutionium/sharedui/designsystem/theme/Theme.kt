package com.solutionium.sharedui.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class WooBrand {
    SiteA,
    SiteB,
}

val LocalWooBrand = staticCompositionLocalOf { WooBrand.SiteA }
val LocalAppLanguage = staticCompositionLocalOf { "en" }

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

private val siteBLightScheme = lightScheme.copy(
    primary = Color(0xFF005E2E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF00874A),
    onPrimaryContainer = Color(0xFFE8FFE8),
    secondary = Color(0xFF2E7D32),
    onSecondary = Color.White,
    tertiary = Color(0xFF00696B),
    onTertiary = Color.White,
)

private val siteBDarkScheme = darkScheme.copy(
    primary = Color(0xFF63DC93),
    onPrimary = Color(0xFF003919),
    primaryContainer = Color(0xFF005228),
    onPrimaryContainer = Color(0xFF8FF9AE),
    secondary = Color(0xFFA4D6A7),
    onSecondary = Color(0xFF0E3B16),
    tertiary = Color(0xFF80D5D7),
    onTertiary = Color(0xFF003738),
)

@Composable
@Suppress("UNUSED_PARAMETER")
fun WooTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    brand: WooBrand = WooBrand.SiteA,
    languageCode: String = "en",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (brand) {
        WooBrand.SiteA -> if (darkTheme) darkScheme else lightScheme
        WooBrand.SiteB -> if (darkTheme) siteBDarkScheme else siteBLightScheme
    }

    CompositionLocalProvider(
        LocalWooBrand provides brand,
        LocalAppLanguage provides languageCode.lowercase()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = wooTypography(),
            content = content,
        )
    }
}
