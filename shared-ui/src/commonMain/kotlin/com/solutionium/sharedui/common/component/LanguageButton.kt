package com.solutionium.sharedui.common.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LanguageButton(
    text: String,
    languageCode: String,
    isSelected: Boolean,
    onClick: (String) -> Unit
) {
    val colors = if (isSelected) {
        // Highlighted state: Filled with primary color
        ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        // Default state: Transparent with primary text
        ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }

    OutlinedButton(
        onClick = { onClick(languageCode) },
        modifier = Modifier.fillMaxWidth(),
        colors = colors,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(text)
    }
}