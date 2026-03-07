package com.solutionium.sharedui.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

expect object SharedUiFonts {
    @Composable
    fun primary(): FontFamily
}
