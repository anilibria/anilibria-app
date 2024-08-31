package ru.radiationx.data.apinext

import anilibria.api.shared.UserResponse
import anilibria.api.shared.release.ReleaseEpisodeResponse
import anilibria.api.shared.release.ReleaseMemberResponse
import anilibria.api.shared.release.ReleaseNameResponse
import anilibria.api.shared.release.ReleaseResponse
import anilibria.api.shared.release.ReleaseSponsorResponse
import anilibria.api.shared.release.ReleaseTorrentResponse
import ru.radiationx.data.apinext.models.ReleaseMember
import ru.radiationx.data.apinext.models.ReleaseName
import ru.radiationx.data.apinext.models.ReleaseSponsor
import ru.radiationx.data.apinext.models.User
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.release.ExternalEpisode
import ru.radiationx.data.entity.domain.release.ExternalPlaylist
import ru.radiationx.data.entity.domain.release.PlayerSkips
import ru.radiationx.data.entity.domain.release.QualityInfo
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.RutubeEpisode
import ru.radiationx.data.entity.domain.release.TorrentItem
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.domain.types.TorrentId
import ru.radiationx.data.entity.mapper.secToMillis

fun ReleaseNameResponse.toDomain(): ReleaseName {
    return ReleaseName(main = main, english = english, alternative = alternative)
}

fun ReleaseMemberResponse.toDomain(): ReleaseMember {
    return ReleaseMember(
        id = id,
        user = user.toDomain(),
        role = role.toDomain(),
        nickname = nickname
    )
}

fun ReleaseMemberResponse.Role.toDomain(): ReleaseMember.Role {
    return ReleaseMember.Role(value = value, description = description)
}

fun UserResponse.toDomain(): User {
    return User(id = id, nickname = nickname, avatar = avatar.src)
}

fun ReleaseSponsorResponse.toDomain(): ReleaseSponsor {
    return ReleaseSponsor(
        id = id,
        title = title,
        description = description,
        urlTitle = urlTitle,
        url = url
    )
}

fun ReleaseEpisodeResponse.toEpisode(releaseId: ReleaseId): Episode? {
    if (hls480 == null && hls720 == null && hls1080 == null) {
        return null
    }
    return Episode(
        id = EpisodeId(
            id = id,
            releaseId = releaseId
        ),
        title = toEpisodeTitle(),
        qualityInfo = QualityInfo(
            urlSd = hls480,
            urlHd = hls720,
            urlFullHd = hls1080
        ),
        updatedAt = updatedAt.apiDateToDate(),
        skips = toPlayerSkips()
    )
}

fun ReleaseEpisodeResponse.toRutubeEpisode(releaseId: ReleaseId): RutubeEpisode? {
    val safeRutubeId = rutubeId ?: return null
    return RutubeEpisode(
        id = EpisodeId(
            id = id,
            releaseId = releaseId
        ),
        title = toEpisodeTitle(),
        updatedAt = updatedAt.apiDateToDate(),
        rutubeId = safeRutubeId,
        url = "https://rutube.ru/play/embed/$safeRutubeId"
    )
}

fun ReleaseResponse.toYoutubePlaylist(releaseId: ReleaseId): ExternalPlaylist? {
    val episodes = episodes?.mapNotNull { it.toYoutubeEpisode(releaseId) }.orEmpty()
    if (episodes.isEmpty()) {
        return null
    }
    return ExternalPlaylist(
        tag = "youtube",
        title = "YouTube",
        actionText = "Смотреть",
        episodes = episodes
    )
}

fun ReleaseEpisodeResponse.toYoutubeEpisode(releaseId: ReleaseId): ExternalEpisode? {
    val safeYoutubeId = youtubeId ?: return null
    return ExternalEpisode(
        id = EpisodeId(
            id = id,
            releaseId = releaseId
        ),
        title = toEpisodeTitle(),
        url = "https://www.youtube.com/watch?v=$safeYoutubeId"
    )
}

fun ReleaseEpisodeResponse.toEpisodeTitle(): String {
    val title = "Серия $ordinal"
    val anyName = name ?: nameEnglish
    return listOfNotNull(title, anyName).joinToString(" • ")
}

private fun ReleaseEpisodeResponse.toPlayerSkips(): PlayerSkips {
    return PlayerSkips(opening = opening.toDomain(), ending = ending.toDomain())
}

private fun ReleaseEpisodeResponse.Skip.toDomain(): PlayerSkips.Skip? {
    val skipStart = start?.secToMillis() ?: return null
    val skipEnd = stop?.secToMillis() ?: return null
    return PlayerSkips.Skip(start = skipStart, end = skipEnd)
}

fun ReleaseTorrentResponse.toDomain(releaseId: ReleaseId): TorrentItem {
    return TorrentItem(
        id = TorrentId(
            id = id,
            releaseId = releaseId
        ),
        hash = hash,
        leechers = leechers,
        seeders = seeders,
        completed = completedTimes,
        quality = quality.description,
        codec = codec.description,
        color = color.description,
        series = description,
        size = size,
        date = updatedAt.apiDateToDate()
    )
}

fun ReleaseResponse.toDomain(): Release {
    val releaseId = ReleaseId(id = id)
    return Release(
        id = releaseId,
        code = ReleaseCode(code = alias),
        names = name.toDomain(),
        poster = poster.src,
        createdAt = createdAt.apiDateToDate(),
        freshAt = createdAt.apiDateToDate(),
        updatedAt = freshAt.apiDateToDate(),
        isOngoing = isOngoing,
        isInProduction = isInProduction,
        type = type.description,
        genres = genres.map { it.name },
        year = year,
        season = season.description,
        publishDay = publishDay.value,
        description = description,
        announce = notification,
        favoritesCount = addedInUsersFavorites,
        ageRating = ageRating.description,
        episodesTotal = episodesTotal,
        isEpisodesCountUnknown = episodesAreUnknown,
        averageEpisodeDuration = averageDurationOfEpisode,
        isBlockedByGeo = isBlockedByGeo,
        isBlockedByCopyrights = isBlockedByCopyrights,
        webPlayer = externalPlayer.ifEmpty { null },

        members = members?.map { it.toDomain() }.orEmpty(),
        sponsor = sponsor?.toDomain(),
        episodes = episodes?.mapNotNull { it.toEpisode(releaseId) }.orEmpty(),
        externalPlaylists = listOfNotNull(toYoutubePlaylist(releaseId)),
        rutubePlaylist = episodes?.mapNotNull { it.toRutubeEpisode(releaseId) }.orEmpty(),
        torrents = torrents?.map { it.toDomain(releaseId) }.orEmpty(),
    )
}