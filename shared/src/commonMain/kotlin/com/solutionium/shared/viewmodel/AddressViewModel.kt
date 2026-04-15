package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.Address
import com.solutionium.shared.domain.user.DeleteAddressUseCase
import com.solutionium.shared.domain.user.LoadAddressesUseCase
import com.solutionium.shared.domain.user.SaveAddressUseCase
import com.solutionium.shared.domain.user.SetDefaultAddressUseCase
import com.solutionium.shared.util.PhoneNumberFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddressViewModel(
    private val saveAddressUseCase: SaveAddressUseCase,
    private val loadAddressUseCase: LoadAddressesUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase,
    private val setAsDefaultAddressUseCase: SetDefaultAddressUseCase,
    args: Map<String, String> = emptyMap(),
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(AddressUiState())
    val state = _state.asStateFlow()

    private val _listState = MutableStateFlow(AddressListUiState())
    val listState = _listState.asStateFlow()

    init {
        val addressIdArg = args["address_id_or_new"]?.toIntOrNull()
        if (addressIdArg != null && addressIdArg != -1) {
            loadAddress(addressIdArg)
        } else {
            _state.update { it.copy(addressId = null) }
        }
        loadAddresses()
    }

    private fun loadAddresses() {
        scope.launch {
            _listState.update { it.copy(isLoading = true) }
            loadAddressUseCase().collect { addresses ->
                _listState.update { it.copy(addresses = addresses, isLoading = false) }
            }
        }
    }

    private fun loadAddress(addressId: Int) {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            loadAddressUseCase(addressId = addressId).collect { address ->
                if (address != null) {
                    _state.update {
                        it.copy(
                            addressId = address.id,
                            title = address.title,
                            firstName = address.firstName,
                            lastName = address.lastName,
                            state = address.state,
                            city = address.city,
                            addressLine1 = address.address1,
                            addressLine2 = address.address2,
                            postalCode = address.postcode,
                            phoneNumber = address.phone,
                            isLoading = false,
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, generalError = "Address not found.") }
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _state.update { it.copy(title = newTitle, errorMessages = it.errorMessages - AddressField.TITLE) }
    }

    fun onFirstNameChange(value: String) {
        _state.update { it.copy(firstName = value, errorMessages = it.errorMessages - AddressField.FIRST_NAME) }
    }

    fun onLastNameChange(value: String) {
        _state.update { it.copy(lastName = value, errorMessages = it.errorMessages - AddressField.LAST_NAME) }
    }

    fun onStateChange(value: String) {
        _state.update { it.copy(state = value, errorMessages = it.errorMessages - AddressField.STATE) }
    }

    fun onCityChange(value: String) {
        _state.update { it.copy(city = value, errorMessages = it.errorMessages - AddressField.CITY) }
    }

    fun onAddressLine1Change(value: String) {
        _state.update { it.copy(addressLine1 = value, errorMessages = it.errorMessages - AddressField.ADDRESS_LINE_1) }
    }

    fun onAddressLine2Change(value: String) {
        _state.update { it.copy(addressLine2 = value) }
    }

    fun onPostalCodeChange(value: String) {
        _state.update { it.copy(postalCode = value, errorMessages = it.errorMessages - AddressField.POSTAL_CODE) }
    }

    fun onPhoneNumberChange(value: String) {
        _state.update { it.copy(phoneNumber = value, errorMessages = it.errorMessages - AddressField.PHONE_NUMBER) }
    }

    fun saveAddress(
        onSuccess: () -> Unit,
        isSiteB: Boolean = false,
    ) {
        if (!validateFields(isSiteB)) return
        _state.update { it.copy(isSaving = true, generalError = null) }
        scope.launch {
            val uiState = _state.value
            val normalizedPhone = uiState.phoneNumber?.trim().orEmpty()
            val addressToSave = Address(
                id = uiState.addressId,
                title = uiState.title,
                firstName = uiState.firstName.trim(),
                lastName = uiState.lastName.trim(),
                state = uiState.state.trim(),
                city = uiState.city.trim(),
                address1 = uiState.addressLine1.trim(),
                address2 = if (isSiteB) null else uiState.addressLine2?.trim()?.ifBlank { null },
                postcode = if (isSiteB) "" else uiState.postalCode.trim(),
                phone = normalizedPhone,
                company = null,
                country = "",
                email = null,
                isDefault = false,
            )

            try {
                saveAddressUseCase(addressToSave)
                _state.update { it.copy(isSaving = false) }
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, generalError = "Failed to save address: ${e.message}") }
            }
        }
    }

    private fun validateFields(isSiteB: Boolean): Boolean {
        val errors = mutableMapOf<String, AddressValidationError>()
        val uiState = _state.value
        if (uiState.firstName.isBlank()) errors[AddressField.FIRST_NAME] = AddressValidationError.FIRST_NAME_EMPTY
        if (uiState.lastName.isBlank()) errors[AddressField.LAST_NAME] = AddressValidationError.LAST_NAME_EMPTY
        if (uiState.state.isBlank()) errors[AddressField.STATE] = AddressValidationError.STATE_EMPTY
        if (uiState.city.isBlank()) errors[AddressField.CITY] = AddressValidationError.CITY_EMPTY
        if (uiState.addressLine1.isBlank()) errors[AddressField.ADDRESS_LINE_1] = AddressValidationError.ADDRESS_LINE_EMPTY
        if (!isSiteB && uiState.postalCode.isBlank()) errors[AddressField.POSTAL_CODE] = AddressValidationError.POSTAL_CODE_EMPTY
        val phone = uiState.phoneNumber?.trim().orEmpty()
        if (phone.isBlank() || !PhoneNumberFormatter.isCanonical(phone)) {
            errors[AddressField.PHONE_NUMBER] = AddressValidationError.INVALID_PHONE
        }
        _state.update { it.copy(errorMessages = errors) }
        return errors.isEmpty()
    }

    fun requestDeleteAddress(address: Address) {
        _listState.update { it.copy(addressToDelete = address, showDeleteConfirmationDialog = true) }
    }

    fun confirmDeleteAddress() {
        scope.launch {
            try {
                deleteAddressUseCase(_listState.value.addressToDelete ?: return@launch)
                _listState.update {
                    it.copy(addressToDelete = null, showDeleteConfirmationDialog = false)
                }
            } catch (e: Exception) {
                _listState.update { it.copy(generalError = "Failed to delete address: ${e.message}") }
            }
        }
    }

    fun cancelDeleteAddress() {
        _listState.update { it.copy(addressToDelete = null, showDeleteConfirmationDialog = false) }
    }

    fun setAsDefaultClicked(id: Int?, isDefault: Boolean) {
        if (isDefault || id == null) return
        scope.launch {
            try {
                setAsDefaultAddressUseCase(id)
            } catch (e: Exception) {
                _listState.update { it.copy(generalError = "Failed to set default address: ${e.message}") }
            }
        }
    }

    fun clear() {
        scope.cancel()
    }
}
