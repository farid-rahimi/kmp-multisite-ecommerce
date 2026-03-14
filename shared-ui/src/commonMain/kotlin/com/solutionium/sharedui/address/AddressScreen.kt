package com.solutionium.sharedui.address

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.solutionium.sharedui.common.component.PlatformTopBar
import com.solutionium.sharedui.common.component.platformPrimaryButtonShape
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.an_unexpected_error_occurred
import com.solutionium.sharedui.resources.add_new_address
import com.solutionium.sharedui.resources.address_line_1
import com.solutionium.sharedui.resources.address_line_2_optional
import com.solutionium.sharedui.resources.address_line_cannot_be_empty
import com.solutionium.sharedui.resources.address_title_optional
import com.solutionium.sharedui.resources.address_title_support_text
import com.solutionium.sharedui.resources.cancel
import com.solutionium.sharedui.resources.city
import com.solutionium.sharedui.resources.city_cannot_be_empty
import com.solutionium.sharedui.resources.default_address
import com.solutionium.sharedui.resources.delete
import com.solutionium.sharedui.resources.delete_address
import com.solutionium.sharedui.resources.delete_address_text
import com.solutionium.sharedui.resources.edit
import com.solutionium.sharedui.resources.edit_address
import com.solutionium.sharedui.resources.enter_a_valid_phone_number
import com.solutionium.sharedui.resources.first_name
import com.solutionium.sharedui.resources.first_name_cannot_be_empty
import com.solutionium.sharedui.resources.last_name
import com.solutionium.sharedui.resources.last_name_cannot_be_empty
import com.solutionium.sharedui.resources.my_addresses
import com.solutionium.sharedui.resources.no_address_yet
import com.solutionium.sharedui.resources.phone
import com.solutionium.sharedui.resources.phone_number
import com.solutionium.sharedui.resources.postal_code_cannot_be_empty
import com.solutionium.sharedui.resources.postal_code_title
import com.solutionium.sharedui.resources.save_address
import com.solutionium.sharedui.resources.state_cannot_be_empty
import com.solutionium.sharedui.resources.state_province
import com.solutionium.shared.data.model.Address
import com.solutionium.shared.viewmodel.AddressField
import com.solutionium.shared.viewmodel.AddressValidationError
import com.solutionium.shared.viewmodel.AddressViewModel
import org.jetbrains.compose.resources.stringResource
import kotlin.text.isNullOrBlank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressListScreen(
    onNavigateToEditAddress: (addressId: Int?) -> Unit,
    onBackNavigation: () -> Unit,
    viewModel: AddressViewModel,
) {
    val uiState by viewModel.listState.collectAsState()

    Scaffold(
        topBar = {
            PlatformTopBar(
                title = { Text(stringResource(Res.string.my_addresses)) },
                onBack = onBackNavigation,
                actions = {
                    IconButton(onClick = { onNavigateToEditAddress(null) }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(Res.string.add_new_address),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.generalError != null -> {
                    Text(
                        text = uiState.generalError ?: stringResource(Res.string.an_unexpected_error_occurred),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                    )
                }

                uiState.addresses.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center),
                    ) {
                        Text(
                            text = stringResource(Res.string.no_address_yet),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp),
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(onClick = { onNavigateToEditAddress(null) }) {
                            Icon(
                                Icons.Filled.Add,
                                stringResource(Res.string.add_new_address),
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(Res.string.add_new_address))
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.addresses) { address ->
                            AddressListItem(
                                address = address,
                                onClick = { viewModel.setAsDefaultClicked(address.id, address.isDefault) },
                                onEditClick = { onNavigateToEditAddress(address.id) },
                                onDeleteClick = { viewModel.requestDeleteAddress(address) },
                            )
                        }
                    }
                }
            }

            if (uiState.showDeleteConfirmationDialog && uiState.addressToDelete != null) {
                DeleteConfirmationDialog(
                    addressTitle =
                        if (uiState.addressToDelete?.title.isNullOrBlank()) {
                            "${uiState.addressToDelete?.firstName ?: ""} ${uiState.addressToDelete?.lastName ?: ""}"
                        } else {
                            uiState.addressToDelete?.title.orEmpty()
                        },
                    onConfirmDelete = { viewModel.confirmDeleteAddress() },
                    onDismiss = { viewModel.cancelDeleteAddress() },
                )
            }
        }
    }
}

@Composable
fun AddressListItem(
    address: Address,
    onClick: () -> Unit = {},
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor =
                if (address.isDefault) {
                    MaterialTheme.colorScheme.surfaceContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                },
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Row {
                if (!address.title.isNullOrBlank()) {
                    Text(
                        text = address.title.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(8.dp))
                }

                Spacer(Modifier.weight(1f))
                if (address.isDefault) {
                    Text(
                        text = stringResource(Res.string.default_address),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Text(
                text = "${address.firstName} ${address.lastName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = address.address1 + (address.address2?.let { "\n$it" } ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
            Text(
                text = "${address.state}, ${address.city} ${address.postcode}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
            Text(
                text = stringResource(Res.string.phone, address.phone ?: "-"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.heightIn(min = 36.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.7f)),
                ) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.heightIn(min = 36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(Res.string.edit))
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    addressTitle: String,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.delete_address)) },
        text = { Text(stringResource(Res.string.delete_address_text, addressTitle)) },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) {
                Text(stringResource(Res.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
        shape = RoundedCornerShape(16.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAddressScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddressViewModel,
) {
    val uiState by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            PlatformTopBar(
                title = {
                    Text(
                        if (uiState.addressId == null) {
                            stringResource(Res.string.add_new_address)
                        } else {
                            stringResource(Res.string.edit_address)
                        },
                    )
                },
                onBack = onBack,
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { viewModel.saveAddress(onSuccess = onSaved) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    enabled = !uiState.isSaving && !uiState.isLoading,
                    shape = platformPrimaryButtonShape(),
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(stringResource(Res.string.save_address))
                    }
                }
            }
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            uiState.generalError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            AddressTextField(
                value = uiState.title ?: "",
                onValueChange = viewModel::onTitleChange,
                label = stringResource(Res.string.address_title_optional),
                supportingText = stringResource(Res.string.address_title_support_text),
                imeAction = ImeAction.Next,
                isRequired = false,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AddressTextField(
                    modifier = Modifier.weight(1f),
                    value = uiState.firstName,
                    onValueChange = viewModel::onFirstNameChange,
                    label = stringResource(Res.string.first_name),
                    errorMessage = uiState.errorMessages[AddressField.FIRST_NAME],
                    imeAction = ImeAction.Next,
                )
                AddressTextField(
                    modifier = Modifier.weight(1f),
                    value = uiState.lastName,
                    onValueChange = viewModel::onLastNameChange,
                    label = stringResource(Res.string.last_name),
                    errorMessage = uiState.errorMessages[AddressField.LAST_NAME],
                    imeAction = ImeAction.Next,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AddressTextField(
                    modifier = Modifier.weight(1f),
                    value = uiState.state,
                    onValueChange = viewModel::onStateChange,
                    label = stringResource(Res.string.state_province),
                    errorMessage = uiState.errorMessages[AddressField.STATE],
                    imeAction = ImeAction.Next,
                )
                AddressTextField(
                    modifier = Modifier.weight(1f),
                    value = uiState.city,
                    onValueChange = viewModel::onCityChange,
                    label = stringResource(Res.string.city),
                    errorMessage = uiState.errorMessages[AddressField.CITY],
                    imeAction = ImeAction.Next,
                )
            }

            AddressTextField(
                value = uiState.addressLine1,
                onValueChange = viewModel::onAddressLine1Change,
                label = stringResource(Res.string.address_line_1),
                errorMessage = uiState.errorMessages[AddressField.ADDRESS_LINE_1],
                imeAction = ImeAction.None,
                singleLine = false,
            )
            AddressTextField(
                value = uiState.addressLine2 ?: "",
                onValueChange = viewModel::onAddressLine2Change,
                label = stringResource(Res.string.address_line_2_optional),
                imeAction = ImeAction.Next,
                isRequired = false,
                singleLine = false,
            )
            AddressTextField(
                value = uiState.postalCode,
                onValueChange = viewModel::onPostalCodeChange,
                label = stringResource(Res.string.postal_code_title),
                errorMessage = uiState.errorMessages[AddressField.POSTAL_CODE],
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            )
            AddressTextField(
                value = uiState.phoneNumber,
                onValueChange = viewModel::onPhoneNumberChange,
                label = stringResource(Res.string.phone_number),
                errorMessage = uiState.errorMessages[AddressField.PHONE_NUMBER],
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
            )

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun AddressTextField(
    value: String?,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorMessage: AddressValidationError? = null,
    supportingText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    isRequired: Boolean = true,
    singleLine: Boolean = true,
) {
    val effectiveLabel = if (isRequired) "$label*" else label
    OutlinedTextField(
        value = value ?: "",
        onValueChange = onValueChange,
        label = { Text(effectiveLabel) },
        modifier = modifier.fillMaxWidth(),
        isError = errorMessage != null,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
        ),
        singleLine = singleLine,
        supportingText = {
            if (errorMessage != null) {
                Text(
                    stringResource(errorMessage.toStringRes()),
                    color = MaterialTheme.colorScheme.error,
                )
            } else if (supportingText != null) {
                Text(supportingText)
            }
        },
    )
}

private fun AddressValidationError.toStringRes() = when (this) {
    AddressValidationError.FIRST_NAME_EMPTY -> Res.string.first_name_cannot_be_empty
    AddressValidationError.LAST_NAME_EMPTY -> Res.string.last_name_cannot_be_empty
    AddressValidationError.STATE_EMPTY -> Res.string.state_cannot_be_empty
    AddressValidationError.CITY_EMPTY -> Res.string.city_cannot_be_empty
    AddressValidationError.ADDRESS_LINE_EMPTY -> Res.string.address_line_cannot_be_empty
    AddressValidationError.POSTAL_CODE_EMPTY -> Res.string.postal_code_cannot_be_empty
    AddressValidationError.INVALID_PHONE -> Res.string.enter_a_valid_phone_number
}
