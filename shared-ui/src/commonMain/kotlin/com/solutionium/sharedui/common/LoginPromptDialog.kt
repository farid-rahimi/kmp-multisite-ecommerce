package com.solutionium.sharedui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.Login_required_text
import com.solutionium.sharedui.resources.cancel
import com.solutionium.sharedui.resources.go_to_login
import com.solutionium.sharedui.resources.login_required
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginPromptDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Person, contentDescription = stringResource(Res.string.login_required)) },
        title = { Text(text = stringResource(Res.string.login_required)) },
        text = { Text(text = stringResource(Res.string.Login_required_text)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.go_to_login))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}
