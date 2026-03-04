package com.solutionium.feature.address


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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solutionium.shared.data.model.Address
import com.solutionium.shared.viewmodel.AddressField
import com.solutionium.shared.viewmodel.AddressValidationError
import com.solutionium.shared.viewmodel.AddressViewModel
import kotlin.text.isNullOrBlank


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressListScreen(

    onNavigateToEditAddress: (addressId: Int?) -> Unit,
    onBackNavigation: () -> Unit,
    viewModel: AddressViewModel // Assumes Hilt or default factory
) {
    val uiState by viewModel.listState.collectAsStateWithLifecycle()

    // This would typically listen for results from the AddEditAddressScreen
    // For example, if AddEditAddressScreen sets a result in NavController's SavedStateHandle:
    // val currentBackStackEntry = LocalNavController.current.currentBackStackEntry
    // LaunchedEffect(currentBackStackEntry) {
    //     currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("address_list_needs_refresh")
    //         ?.observe(currentBackStackEntry as LifecycleOwner) { needsRefresh ->
    //             if (needsRefresh) {
    //                 viewModel.refreshAddressList()
    //                 currentBackStackEntry.savedStateHandle.remove<Boolean>("address_list_needs_refresh")
    //             }
    //         }
    // }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_addresses)) },
                navigationIcon = {
                    IconButton(onClick = onBackNavigation) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEditAddress(null) }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_new_address))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.generalError != null) {
                Text(
                    text = uiState.generalError ?: stringResource(R.string.unexpected_error_occurred),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else if (uiState.addresses.isEmpty()) {
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {

                    Text(
                        text = stringResource(R.string.no_address_yet),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { onNavigateToEditAddress(null) },
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            stringResource(R.string.add_new_address),
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(
                            stringResource(R.string.add_new_address),
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(all = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.addresses) { address ->
                        AddressListItem(
                            address = address,
                            onClick = { viewModel.setAsDefaultClicked(address.id, address.isDefault) },
                            onEditClick = { onNavigateToEditAddress(address.id) },
                            onDeleteClick = { viewModel.requestDeleteAddress(address) }
                        )
                    }
                }
            }

            if (uiState.showDeleteConfirmationDialog && uiState.addressToDelete != null) {
                val currentLocale = LocalConfiguration.current.locales[0]
                key(currentLocale) {
                    DeleteConfirmationDialog(
//                    addressTitle = if (uiState.addressToDelete?.title.isNullOrBlank())
//                         (uiState.addressToDelete?.firstName + " " + uiState.addressToDelete?.lastName) else uiState.addressToDelete?.title!!,
                        addressTitle = "test",
                        onConfirmDelete = { viewModel.confirmDeleteAddress() },
                        onDismiss = { viewModel.cancelDeleteAddress() }
                    )
                }
            }
        }
    }
}

@Composable
fun AddressListItem(
    modifier: Modifier = Modifier,
    address: Address,
    onClick: () -> Unit = {},
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = if (address.isDefault) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row {
                if (!address.title.isNullOrBlank()) {
                    Text(
                        text = address.title ?: "",
                        style = MaterialTheme.typography.titleLarge, // Make title prominent
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                }

                Spacer(Modifier.weight(1f)) // Pushes "Default Address" to the right
                if (address.isDefault) {
                    Text(
                        text = stringResource(R.string.default_address),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = address.firstName + " " + address.lastName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = address.address1 + (address.address2?.let { "\n$it" } ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = "${address.state}, ${address.city} ${address.postcode}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = "Phone: ${address.phone}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End, // Place buttons to the right
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton( // Subtle delete button
                    onClick = onDeleteClick,
                    modifier = Modifier.heightIn(min = 36.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                ) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    // Spacer(modifier = Modifier.width(4.dp))
                    // Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button( // Prominent edit button
                    onClick = onEditClick,
                    modifier = Modifier.heightIn(min = 36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.edit))
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    addressTitle: String,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_address)) },
        text = { Text(stringResource(R.string.delete_address_text, addressTitle)) },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAddressScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddressViewModel // Assumes Hilt or default factory
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.addressId == null) stringResource(R.string.add_new_address) else stringResource(
                    R.string.edit_address
                )
                ) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) { // Sticky save button
                Button(
                    onClick = { viewModel.saveAddress(onSuccess = onSaved) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    enabled = !uiState.isSaving && !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.save_address))
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = 16.dp, vertical = 8.dp) // Inner padding for form
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.generalError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            AddressTextField(
                value = uiState.title ?: "",
                onValueChange = viewModel::onTitleChange,
                label = stringResource(R.string.address_title_optional), // e.g., "Address Title (Optional)"
                supportingText = stringResource(R.string.address_title_support_text),
                imeAction = ImeAction.Next,
                isRequired = false
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AddressTextField(
                    modifier = Modifier.weight(1f),
                    value = uiState.firstName,
                    onValueChange = viewModel::onFirstNameChange,
                    label = stringResource(R.string.first_name),
                    errorMessage = uiState.errorMessages[AddressField.FIRST_NAME],
                    imeAction = ImeAction.Next
                )
                AddressTextField(
                    modifier = Modifier.weight(1f),
                    value = uiState.lastName,
                    onValueChange = viewModel::onLastNameChange,
                    label = stringResource(R.string.last_name),
                    errorMessage = uiState.errorMessages[AddressField.LAST_NAME],
                    imeAction = ImeAction.Next
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AddressTextField(
                    modifier = Modifier.weight(1f),
                    value = uiState.state,
                    onValueChange = viewModel::onStateChange,
                    label = stringResource(R.string.state_province),
                    errorMessage = uiState.errorMessages[AddressField.STATE],
                    imeAction = ImeAction.Next
                )
                AddressTextField(
                    modifier = Modifier.weight(1f),
                    value = uiState.city,
                    onValueChange = viewModel::onCityChange,
                    label = stringResource(R.string.city),
                    errorMessage = uiState.errorMessages[AddressField.CITY],
                    imeAction = ImeAction.Next
                )
            }
            AddressTextField(
                value = uiState.addressLine1,
                onValueChange = viewModel::onAddressLine1Change,
                label = stringResource(R.string.address_line_1),
                errorMessage = uiState.errorMessages[AddressField.ADDRESS_LINE_1],
                imeAction = ImeAction.None,
                singleLine = false
            )
            AddressTextField(
                value = uiState.addressLine2 ?: "",
                onValueChange = viewModel::onAddressLine2Change,
                label = stringResource(R.string.address_line_2_optional),
                imeAction = ImeAction.Next,
                isRequired = false,
                singleLine = false
            )
            AddressTextField(
                value = uiState.postalCode,
                onValueChange = viewModel::onPostalCodeChange,
                label = stringResource(R.string.postal_code_title),
                errorMessage = uiState.errorMessages[AddressField.POSTAL_CODE],
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
            AddressTextField(
                value = uiState.phoneNumber,
                onValueChange = viewModel::onPhoneNumberChange,
                label = stringResource(R.string.phone_number),
                errorMessage = uiState.errorMessages[AddressField.PHONE_NUMBER],
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(60.dp)) // Space for sticky button at the end of scroll
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
    singleLine: Boolean = true
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
            imeAction = imeAction
        ),
        singleLine = singleLine,
        supportingText = {
            if (errorMessage != null)
                Text(
                    stringResource(errorMessage.toStringRes()),
                    color = MaterialTheme.colorScheme.error,
                )
            else if (supportingText != null)
                Text(supportingText)

        }
    )
}

private fun AddressValidationError.toStringRes(): Int = when (this) {
    AddressValidationError.FIRST_NAME_EMPTY -> R.string.first_name_cannot_be_empty
    AddressValidationError.LAST_NAME_EMPTY -> R.string.last_name_cannot_be_empty
    AddressValidationError.STATE_EMPTY -> R.string.state_cannot_be_empty
    AddressValidationError.CITY_EMPTY -> R.string.city_cannot_be_empty
    AddressValidationError.ADDRESS_LINE_EMPTY -> R.string.address_line_cannot_be_empty
    AddressValidationError.POSTAL_CODE_EMPTY -> R.string.postal_code_cannot_be_empty
    AddressValidationError.INVALID_PHONE -> R.string.enter_a_valid_phone_number
}
