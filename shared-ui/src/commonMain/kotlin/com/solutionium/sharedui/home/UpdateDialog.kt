package com.solutionium.sharedui.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.contact_support
import com.solutionium.sharedui.resources.force_update_text
import com.solutionium.sharedui.resources.later
import com.solutionium.sharedui.resources.update_available
import com.solutionium.sharedui.resources.update_now
import com.solutionium.sharedui.resources.update_text
import com.solutionium.shared.viewmodel.UpdateInfo
import com.solutionium.shared.viewmodel.UpdateType
import org.jetbrains.compose.resources.stringResource

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit,
    onContactSupportClick: () -> Unit,
    onUpdateNowClick: () -> Unit,
) {
    val isForced = updateInfo.type == UpdateType.FORCED

    AlertDialog(
        onDismissRequest = {
            if (!isForced) onDismiss()
        },
        title = { Text(stringResource(Res.string.update_available)) },
        text = {
            val message = if (isForced) {
                stringResource(Res.string.force_update_text, updateInfo.latestVersionName)
            } else {
                stringResource(Res.string.update_text, updateInfo.latestVersionName)
            }
            Text(message)
        },
        confirmButton = {
            Button(onClick = onUpdateNowClick) {
                Text(stringResource(Res.string.update_now))
            }
        },
        dismissButton = {
            if (isForced) {
                TextButton(onClick = onContactSupportClick) {
                    Text(stringResource(Res.string.contact_support))
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.later))
                }
            }
        },
    )
}
