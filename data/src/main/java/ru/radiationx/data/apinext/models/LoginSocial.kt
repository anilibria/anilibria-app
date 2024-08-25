package ru.radiationx.data.apinext.models


import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginSocial(
    val url: String,
    val state: SocialState,
    val redirectUrl: String
)