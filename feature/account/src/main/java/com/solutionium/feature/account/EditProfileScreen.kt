package com.solutionium.feature.account

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.solutionium.shared.data.model.UserDetails
import com.solutionium.shared.viewmodel.AccountValidationErrorKeys
import com.solutionium.shared.viewmodel.FieldErrors


// --- Placeholder Sub-Screens for Edit Profile, View Orders, Manage Addresses ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSubScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    userDetails: UserDetails,
    isNewUser: Boolean = false,
    onNavigateBack: (() -> Unit)?,
    onSaveChanges: (UserDetails) -> Unit,
    validationErrors: FieldErrors
) {
    var displayName by remember(userDetails.displayName) { mutableStateOf(userDetails.displayName) }
    var firstName by remember(userDetails.firstName) { mutableStateOf(userDetails.firstName) }
    var lastName by remember(userDetails.lastName) { mutableStateOf(userDetails.lastName) }
    var email by remember(userDetails.email) { mutableStateOf(userDetails.email) }

    BackHandler(enabled = isNewUser) {
        // Do nothing when the back button is pressed, effectively blocking it.
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (isNewUser) stringResource(R.string.complete_your_profile) else stringResource(
                    R.string.edit_profile
                )) },
                navigationIcon = {
                    if (!isNewUser)
                        IconButton(onClick = { onNavigateBack?.invoke() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text(stringResource(R.string.first_name)) },
                    modifier = Modifier.weight(1f),
                    isError = validationErrors.firstNameErrorKey != null,

                    supportingText = {
                        // If there's an error, display the string from the resource ID
                        if (validationErrors.firstNameErrorKey != null) {
                            Text(
                                text = stringResource(id = mapValidationErrorToRes(validationErrors.firstNameErrorKey)),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text(stringResource(R.string.last_name)) },
                    modifier = Modifier.weight(1f),
                    isError = validationErrors.lastNameErrorKey != null,
                    supportingText = {
                        if (validationErrors.lastNameErrorKey != null) {
                            Text(
                                text = stringResource(id = mapValidationErrorToRes(validationErrors.lastNameErrorKey)),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text(stringResource(R.string.display_name)) },
                supportingText = { Text(stringResource(R.string.display_name_suppprt_text)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email_address)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                isError = validationErrors.emailErrorKey != null,
                supportingText = {
                    if (validationErrors.emailErrorKey != null) {
                        Text(
                            text = stringResource(id = mapValidationErrorToRes(validationErrors.emailErrorKey)),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Text(
                stringResource(R.string.phone, userDetails.phoneNumber),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.weight(1f))

//            if (isNewUser)
//                TextButton(
//                    onClick = onNavigateBack
//                ) {
//                    Text(stringResource(R.string.later_button))
//                }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onSaveChanges(
                        userDetails.copy(
                            displayName = displayName,
                            firstName = firstName,
                            lastName = lastName,
                            email = email
                        )
                    )

                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading)
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else
                    Text(stringResource(R.string.save_changes))
            }


        }
    }
}

private fun mapValidationErrorToRes(errorKey: String?): Int {
    return when (errorKey) {
        AccountValidationErrorKeys.FIELD_REQUIRED -> R.string.error_field_required
        AccountValidationErrorKeys.INVALID_EMAIL -> R.string.error_invalid_email
        else -> R.string.error_field_required
    }
}
