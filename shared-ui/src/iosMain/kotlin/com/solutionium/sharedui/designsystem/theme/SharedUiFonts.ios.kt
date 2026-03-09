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
import com.solutionium.sharedui.resources.vazirmatn_black
import com.solutionium.sharedui.resources.vazirmatn_bold
import com.solutionium.sharedui.resources.vazirmatn_light
import com.solutionium.sharedui.resources.vazirmatn_medium
import com.solutionium.sharedui.resources.vazirmatn_regular
import com.solutionium.sharedui.resources.vazirmatn_thin
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font

actual object SharedUiFonts {
    @Composable
    @OptIn(ExperimentalResourceApi::class)
    actual fun primary(languageCode: String): FontFamily = when (languageCode.lowercase()) {
        "fa" -> FontFamily(
        Font(Res.font.vazir_thin, FontWeight.Thin),
        Font(Res.font.vazir_light, FontWeight.ExtraLight),
        Font(Res.font.vazir_light, FontWeight.Light),
        Font(Res.font.vazir_regular, FontWeight.Normal),
        Font(Res.font.vazir_medium, FontWeight.Medium),
        Font(Res.font.vazir_bold, FontWeight.SemiBold),
        Font(Res.font.vazir_bold, FontWeight.Bold),
        Font(Res.font.vazir_black, FontWeight.Black),
    )
        "ar" -> FontFamily(
            Font(Res.font.vazirmatn_thin, FontWeight.Thin),
            Font(Res.font.vazirmatn_light, FontWeight.ExtraLight),
            Font(Res.font.vazirmatn_light, FontWeight.Light),
            Font(Res.font.vazirmatn_regular, FontWeight.Normal),
            Font(Res.font.vazirmatn_medium, FontWeight.Medium),
            Font(Res.font.vazirmatn_bold, FontWeight.SemiBold),
            Font(Res.font.vazirmatn_bold, FontWeight.Bold),
            Font(Res.font.vazirmatn_black, FontWeight.Black),
        )
        else -> FontFamily.SansSerif
    }
}
