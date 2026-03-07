package com.solutionium.sharedui.common.component


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.solutionium.sharedui.designsystem.theme.LocalWooBrand
import com.solutionium.sharedui.designsystem.theme.WooBrand


// In your core UI or a dedicated feature module
@Composable
fun LanguageSelectionScreen(
    currentLang: String = "",
    onLanguageSelected: (String) -> Unit
) {
    val languageOptions = when (LocalWooBrand.current) {
        WooBrand.SiteA -> listOf(
            "en" to "English",
            "fa" to "فارسی",
        )

        WooBrand.SiteB -> listOf(
            "en" to "English",
            "ar" to "العربية",
        )
    }

    Surface(modifier = Modifier
        .fillMaxSize()
        .padding(32.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Choose your language",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(32.dp))
            languageOptions.forEachIndexed { index, (code, label) ->
                LanguageButton(
                    text = label,
                    languageCode = code,
                    isSelected = currentLang == code,
                    onClick = { onLanguageSelected(code) },
                )
                if (index != languageOptions.lastIndex) {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
