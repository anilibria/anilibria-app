package ru.radiationx.data.entity.response.auth

data class SocialAuthResponse(
    val key: String,
    val title: String,
    val socialUrl: String,
    val resultPattern: String,
    val errorUrlPattern: String
)