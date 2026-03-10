package com.solutionium.shared.data.api.woo.converters

import com.solutionium.shared.data.model.ActionType
import com.solutionium.shared.data.model.UserAccess
import com.solutionium.shared.data.model.UserDetails
import com.solutionium.shared.data.network.request.EditUserFCMTokenMeta
import com.solutionium.shared.data.network.request.EditUserRequest
import com.solutionium.shared.data.network.response.DigitsLoginRegisterData
import com.solutionium.shared.data.network.response.WpUserResponse


fun DigitsLoginRegisterData.toUserAccess(): UserAccess = UserAccess(
    userId = userId ?: "",
    token = token ?: "",
    tokenType = tokenType ?: "",
    action = if (action == "register") ActionType.REGISTER else ActionType.LOGIN
)

fun WpUserResponse.toUserDetails(): UserDetails = UserDetails(
    firstName = firstName ?: "",
    lastName = lastName ?: "",
    nickName = nickname ?: "",
    displayName = name ?: "",
    email = email ?: "",
    phoneNumber = phone ?: "",
    isSuperUser = isSuperAdmin ?: false,
    fcmToken = ""
)

fun UserDetails.toEditUserRequest(): EditUserRequest = EditUserRequest(
    firstName = firstName.ifBlank { null },
    lastName = lastName.ifBlank { null },
    nickname = nickName.ifBlank { null },
    name = displayName.ifBlank { null },
    email = email.ifBlank { null },
    phoneNumber = phoneNumber.ifBlank { null },
    meta = if (fcmToken.isNotBlank()) EditUserFCMTokenMeta(fcmToken) else null
)
