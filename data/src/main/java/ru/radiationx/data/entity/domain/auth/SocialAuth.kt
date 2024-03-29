package ru.radiationx.data.entity.domain.auth

data class SocialAuth(
    val key: String,
    val title: String,
    val socialUrl: String,
    val resultPattern: String,
    val errorUrlPattern: String
)