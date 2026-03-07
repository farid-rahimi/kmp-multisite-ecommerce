package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.Address

object AddressField {
    const val TITLE = "title"
    const val FIRST_NAME = "firstName"
    const val LAST_NAME = "lastName"
    const val STATE = "state"
    const val CITY = "city"
    const val ADDRESS_LINE_1 = "addressLine1"
    const val POSTAL_CODE = "postalCode"
    const val PHONE_NUMBER = "phoneNumber"
}

enum class AddressValidationError {
    FIRST_NAME_EMPTY,
    LAST_NAME_EMPTY,
    STATE_EMPTY,
    CITY_EMPTY,
    ADDRESS_LINE_EMPTY,
    POSTAL_CODE_EMPTY,
    INVALID_PHONE,
}

data class AddressUiState(
    val addressId: Int? = null,
    val title: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val state: String = "",
    val city: String = "",
    val addressLine1: String = "",
    val addressLine2: String? = null,
    val postalCode: String = "",
    val phoneNumber: String? = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessages: Map<String, AddressValidationError> = emptyMap(),
    val generalError: String? = null,
)

data class AddressListUiState(
    val addresses: List<Address> = emptyList(),
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val showDeleteConfirmationDialog: Boolean = false,
    val addressToDelete: Address? = null,
)
