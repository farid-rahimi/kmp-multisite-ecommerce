package com.solutionium.sharedui.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.solutionium.sharedui.common.component.platformMaterialShapes

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
    primaryContainer = Color(0xFFC6F2D8),
    onPrimaryContainer = Color(0xFF002113),
    secondary = Color(0xFF2F6B4D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2EADB),
    onSecondaryContainer = Color(0xFF0E2217),
    tertiary = Color(0xFF2D5F64),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFECEF),
    onTertiaryContainer = Color(0xFF0E2022),
    background = Color(0xFFF9FAF8),
    onBackground = Color(0xFF1A1D1B),
    surface = Color(0xFFF9FAF8),
    onSurface = Color(0xFF1A1D1B),
    surfaceVariant = Color(0xFFDEE5DF),
    onSurfaceVariant = Color(0xFF424A44),
    outline = Color(0xFF727B74),
    outlineVariant = Color(0xFFC2CBC4),
    inverseSurface = Color(0xFF2E322F),
    inverseOnSurface = Color(0xFFEFF1EE),
    inversePrimary = Color(0xFF67C98F),
    surfaceDim = Color(0xFFD9DEDA),
    surfaceBright = Color(0xFFF9FAF8),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4F7F4),
    surfaceContainer = Color(0xFFEEF2EE),
    surfaceContainerHigh = Color(0xFFE8ECE8),
    surfaceContainerHighest = Color(0xFFE2E7E3),
    scrim = Color(0x66000000),
)

private val siteBDarkScheme = darkScheme.copy(
    primary = Color(0xFF63DC93),
    onPrimary = Color(0xFF00381A),
    primaryContainer = Color(0xFF005E2E),
    onPrimaryContainer = Color(0xFFC6F2D8),
    secondary = Color(0xFFB5CCBC),
    onSecondary = Color(0xFF21352A),
    secondaryContainer = Color(0xFF374B3F),
    onSecondaryContainer = Color(0xFFD1E9D8),
    tertiary = Color(0xFFA7CDD2),
    onTertiary = Color(0xFF10363A),
    tertiaryContainer = Color(0xFF2A4E52),
    onTertiaryContainer = Color(0xFFC4E9EE),
    background = Color(0xFF111413),
    onBackground = Color(0xFFE0E4E1),
    surface = Color(0xFF111413),
    onSurface = Color(0xFFE0E4E1),
    surfaceVariant = Color(0xFF404943),
    onSurfaceVariant = Color(0xFFBEC8C1),
    outline = Color(0xFF89938C),
    outlineVariant = Color(0xFF404943),
    inverseSurface = Color(0xFFE0E4E1),
    inverseOnSurface = Color(0xFF2D322F),
    inversePrimary = Color(0xFF005E2E),
    surfaceDim = Color(0xFF111413),
    surfaceBright = Color(0xFF363B38),
    surfaceContainerLowest = Color(0xFF0C0F0E),
    surfaceContainerLow = Color(0xFF181B1A),
    surfaceContainer = Color(0xFF1C201E),
    surfaceContainerHigh = Color(0xFF262B28),
    surfaceContainerHighest = Color(0xFF313633),
    scrim = Color(0x99000000),
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
            shapes = platformMaterialShapes(),
            content = content,
        )
    }
}
