package ru.radiationx.data.api.profile.mapper

import anilibria.api.profile.models.ProfileResponse
import ru.radiationx.data.api.profile.db.UserDb
import ru.radiationx.data.api.profile.models.Profile
import ru.radiationx.data.api.profile.models.User
import ru.radiationx.data.api.shared.apiDateToDate
import ru.radiationx.data.common.UserId
import ru.radiationx.data.common.toRelativeUrl

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

fun User.toDb(): UserDb {
    return UserDb(id = id.id, nickname = nickname, avatar = avatar?.raw)
}

fun UserDb.toDomain(): User {
    return User(
        id = UserId(id),
        nickname = nickname,
        avatar = avatar?.toRelativeUrl()
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
        avatar = avatar?.preview?.toRelativeUrl()
    )
}