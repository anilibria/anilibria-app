package ru.radiationx.data.api.auth.models


import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginSocial(
    val url: String,
    val state: SocialState,
    val redirectUrl: String
)