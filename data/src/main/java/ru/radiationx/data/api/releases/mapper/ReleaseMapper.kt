package ru.radiationx.data.api.releases.mapper

import anilibria.api.shared.ReleaseIdNetwork
import anilibria.api.shared.release.ReleaseEpisodeResponse
import anilibria.api.shared.release.ReleaseGenreResponse
import anilibria.api.shared.release.ReleaseMemberResponse
import anilibria.api.shared.release.ReleaseNameResponse
import anilibria.api.shared.release.ReleaseResponse
import anilibria.api.shared.release.ReleaseSponsorResponse
import ru.radiationx.data.api.releases.models.Episode
import ru.radiationx.data.api.releases.models.ExternalEpisode
import ru.radiationx.data.api.releases.models.ExternalPlaylist
import ru.radiationx.data.api.releases.models.PlayerSkips
import ru.radiationx.data.api.releases.models.QualityInfo
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.releases.models.ReleaseCounters
import ru.radiationx.data.api.releases.models.ReleaseGenre
import ru.radiationx.data.api.releases.models.ReleaseMember
import ru.radiationx.data.api.releases.models.ReleaseName
import ru.radiationx.data.api.releases.models.ReleaseSponsor
import ru.radiationx.data.api.releases.models.RutubeEpisode
import ru.radiationx.data.api.schedule.models.PublishDay
import ru.radiationx.data.api.shared.apiDateToDate
import ru.radiationx.data.api.shared.secToMillis
import ru.radiationx.data.api.torrents.mapper.toDomain
import ru.radiationx.data.common.EpisodeId
import ru.radiationx.data.common.GenreId
import ru.radiationx.data.common.ReleaseCode
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.data.common.UserId
import ru.radiationx.data.common.toAbsoluteUrl
import ru.radiationx.data.common.toPathUrl
import java.math.BigDecimal
import java.util.Date


fun ReleaseId.toNetwork(): ReleaseIdNetwork {
    return ReleaseIdNetwork(
        releaseId = id
    )
}

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
    return ReleaseMember.User(id = UserId(id), avatar = avatar?.preview?.toPathUrl())
}

fun ReleaseSponsorResponse.toDomain(): ReleaseSponsor {
    return ReleaseSponsor(
        id = id,
        title = title,
        description = description,
        urlTitle = urlTitle,
        url = url.toAbsoluteUrl()
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
        url = "https://www.youtube.com/watch?v=$safeYoutubeId".toAbsoluteUrl()
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


fun ReleaseGenreResponse.toDomain(): ReleaseGenre = ReleaseGenre(GenreId(id), name)

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
        poster = poster.preview?.toPathUrl(),
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