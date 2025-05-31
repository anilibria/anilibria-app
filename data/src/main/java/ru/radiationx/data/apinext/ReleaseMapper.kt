package ru.radiationx.data.apinext

import anilibria.api.genres.models.GenreResponse
import anilibria.api.shared.release.ReleaseEpisodeResponse
import anilibria.api.shared.release.ReleaseMemberResponse
import anilibria.api.shared.release.ReleaseNameResponse
import anilibria.api.shared.release.ReleaseResponse
import anilibria.api.shared.release.ReleaseSponsorResponse
import anilibria.api.torrent.models.TorrentResponse
import ru.radiationx.data.apinext.models.Genre
import ru.radiationx.data.apinext.models.ReleaseCounters
import ru.radiationx.data.apinext.models.ReleaseMember
import ru.radiationx.data.apinext.models.ReleaseName
import ru.radiationx.data.apinext.models.ReleaseSponsor
import ru.radiationx.data.apinext.models.enums.PublishDay
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.release.ExternalEpisode
import ru.radiationx.data.entity.domain.release.ExternalPlaylist
import ru.radiationx.data.entity.domain.release.PlayerSkips
import ru.radiationx.data.entity.domain.release.QualityInfo
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.RutubeEpisode
import ru.radiationx.data.entity.domain.release.TorrentItem
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.GenreId
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.domain.types.TorrentId
import ru.radiationx.data.entity.domain.types.UserId
import ru.radiationx.data.entity.mapper.secToMillis
import java.math.BigDecimal
import java.util.Date

fun ReleaseNameResponse.toDomain(): ReleaseName {
    return ReleaseName(main = main, english = english, alternative = alternative)
}

fun ReleaseMemberResponse.toDomain(): ReleaseMember {
    return ReleaseMember(
        id = id,
        user = user?.toDomain(),
        role = role.toDomain(),
        nickname = nickname
    )
}

fun ReleaseMemberResponse.Role.toDomain(): ReleaseMember.Role {
    return ReleaseMember.Role(value = value, description = description)
}

fun ReleaseMemberResponse.User.toDomain(): ReleaseMember.User {
    return ReleaseMember.User(id = UserId(id), avatar = avatar?.preview?.toRelativeUrl())
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
    val episodeId = createId(releaseId)
    return Episode(
        id = episodeId,
        title = toEpisodeTitle(episodeId),
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
    val episodeId = createId(releaseId)
    return RutubeEpisode(
        id = episodeId,
        title = toEpisodeTitle(episodeId),
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
    val episodeId = createId(releaseId)
    return ExternalEpisode(
        id = episodeId,
        title = toEpisodeTitle(episodeId),
        url = "https://www.youtube.com/watch?v=$safeYoutubeId"
    )
}

// episode ids can be float/double/int e.g. 25, 25.5.
private fun ReleaseEpisodeResponse.createId(releaseId: ReleaseId): EpisodeId {
    val big = BigDecimal(ordinal)
    return EpisodeId(big.toString(), releaseId)
}

fun ReleaseEpisodeResponse.toEpisodeTitle(episodeId: EpisodeId): String {
    val title = "Серия ${episodeId.id}"
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

fun TorrentResponse.toDomain(releaseId: ReleaseId): TorrentItem {
    return TorrentItem(
        id = TorrentId(
            id = id,
            releaseId = releaseId
        ),
        hash = hash,
        leechers = leechers,
        seeders = seeders,
        completed = completedTimes,
        type = type.description,
        quality = quality.description,
        codec = codec.description,
        color = color.description,
        series = description,
        size = size,
        date = updatedAt.apiDateToDate(),
        magnet = magnet
    )
}

fun GenreResponse.toDomain(): Genre = Genre(GenreId(id), name)

fun ReleaseResponse.toDomainCounters(): ReleaseCounters {
    return ReleaseCounters(
        favorites = addedInUsersFavorites,
        planned = addedInPlannedCollection,
        watched = addedInWatchedCollection,
        watching = addedInWatchingCollection,
        postponed = addedInPostponedCollection,
        abandoned = addedInAbandonedCollection
    )
}

fun ReleaseResponse.toDomain(): Release {
    val releaseId = ReleaseId(id = id)
    return Release(
        id = releaseId,
        code = ReleaseCode(code = alias),
        names = name.toDomain(),
        poster = poster.preview?.toRelativeUrl(),
        createdAt = createdAt?.apiDateToDate() ?: Date(0),
        freshAt = freshAt?.apiDateToDate() ?: Date(0),
        updatedAt = updatedAt?.apiDateToDate() ?: Date(0),
        isOngoing = isOngoing,
        isInProduction = isInProduction,
        type = type.description,
        year = year,
        season = season.description,
        publishDay = PublishDay.ofRaw(publishDay.value),
        description = description,
        announce = notification,
        counters = toDomainCounters(),
        ageRating = ageRating.label,
        episodesTotal = episodesTotal,
        averageEpisodeDuration = averageDurationOfEpisode,
        isBlockedByGeo = isBlockedByGeo,
        isBlockedByCopyrights = isBlockedByCopyrights,
        webPlayer = externalPlayer?.ifEmpty { null },

        genres = genres?.map { it.toDomain() }.orEmpty(),
        members = members?.map { it.toDomain() }.orEmpty(),
        sponsor = sponsor?.toDomain(),
        episodes = episodes?.mapNotNull { it.toEpisode(releaseId) }.orEmpty(),
        externalPlaylists = listOfNotNull(toYoutubePlaylist(releaseId)),
        rutubePlaylist = episodes?.mapNotNull { it.toRutubeEpisode(releaseId) }.orEmpty(),
        torrents = torrents?.map { it.toDomain(releaseId) }.orEmpty(),
    )
}