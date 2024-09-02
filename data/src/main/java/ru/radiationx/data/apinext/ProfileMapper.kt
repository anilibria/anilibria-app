package ru.radiationx.data.apinext

import anilibria.api.profile.models.ProfileResponse
import ru.radiationx.data.apinext.models.Profile
import ru.radiationx.data.apinext.models.User
import ru.radiationx.data.entity.domain.types.UserId

fun ProfileResponse.toDomain(): Profile {
    return Profile(
        user = toDomainUser(),
        login = login,
        email = email,
        torrents = torrents.toDomain(),
        isBanned = isBanned,
        createdAt = createdAt.apiDateToDate(),
        isWithAds = isWithAds
    )
}

private fun ProfileResponse.Torrents.toDomain(): Profile.Torrents {
    return Profile.Torrents(
        passkey = passkey,
        uploaded = uploaded,
        downloaded = downloaded
    )
}

private fun ProfileResponse.toDomainUser(): User {
    return User(
        id = UserId(id),
        nickname = nickname,
        avatar = avatar?.src
    )
}