package ru.radiationx.data.api.profile.models

import java.util.Date

data class Profile(
    val user: User,
    val login: String,
    val email: String,
    val torrents: Torrents,
    val isBanned: Boolean,
    val createdAt: Date,
    val isWithAds: Boolean
) {

    data class Torrents(
        val passkey: String,
        val uploaded: Long,
        val downloaded: Long
    )
}