package com.solutionium.shared.data.network.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditUserRequest(
    val name: String? = null,

    @SerialName("first_name")
    val firstName: String? = null,

    @SerialName("last_name")
    val lastName: String? = null,

    val nickname: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("phone_number")
    val phoneNumber: String? = null,

    @SerialName("meta")
    val meta: EditUserFCMTokenMeta? = null
)

@Serializable
data class EditUserFCMTokenMeta(
    @SerialName("_fcm_token")
    val fcmToken: String? = null
)
