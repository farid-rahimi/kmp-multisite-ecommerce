package com.solutionium.sharedui.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.vazir_black
import com.solutionium.sharedui.resources.vazir_bold
import com.solutionium.sharedui.resources.vazir_light
import com.solutionium.sharedui.resources.vazir_medium
import com.solutionium.sharedui.resources.vazir_regular
import com.solutionium.sharedui.resources.vazir_thin
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font

actual object SharedUiFonts {
    @Composable
    @OptIn(ExperimentalResourceApi::class)
    actual fun primary(): FontFamily = FontFamily(
        Font(Res.font.vazir_thin, FontWeight.Thin),
        Font(Res.font.vazir_light, FontWeight.ExtraLight),
        Font(Res.font.vazir_light, FontWeight.Light),
        Font(Res.font.vazir_regular, FontWeight.Normal),
        Font(Res.font.vazir_medium, FontWeight.Medium),
        Font(Res.font.vazir_bold, FontWeight.SemiBold),
        Font(Res.font.vazir_bold, FontWeight.Bold),
        Font(Res.font.vazir_black, FontWeight.Black),
    )
}
