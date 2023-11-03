package ru.radiationx.data.entity.mapper

import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.domain.release.BlockedInfo
import ru.radiationx.data.entity.domain.release.FavoriteInfo
import ru.radiationx.data.entity.domain.release.Members
import ru.radiationx.data.entity.domain.release.RandomRelease
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.response.release.BlockedInfoResponse
import ru.radiationx.data.entity.response.release.FavoriteInfoResponse
import ru.radiationx.data.entity.response.release.MembersResponse
import ru.radiationx.data.entity.response.release.RandomReleaseResponse
import ru.radiationx.data.entity.response.release.ReleaseResponse
import ru.radiationx.data.system.ApiUtils

fun RandomReleaseResponse.toDomain(): RandomRelease = RandomRelease(
    code = ReleaseCode(code)
)

fun ReleaseResponse.toDomain(
    apiUtils: ApiUtils,
    apiConfig: ApiConfig,
): Release {
    val releaseId = ReleaseId(id)
    return Release(
        id = releaseId,
        code = ReleaseCode(code),
        names = names?.map { apiUtils.escapeHtml(it).toString() }.orEmpty(),
        series = series,
        poster = poster?.appendBaseUrl(apiConfig.baseImagesUrl),
        torrentUpdate = torrentUpdate ?: 0,
        status = status,
        statusCode = statusCode,
        types = type?.let { listOf(it) }.orEmpty(),
        genres = genres.orEmpty(),
        voices = voices.orEmpty(),
        members = members?.toDomain(),
        year = year,
        season = season,
        days = day?.let { listOf(it) }.orEmpty(),
        description = description?.trim(),
        announce = announce?.trim(),
        favoriteInfo = favorite?.toDomain() ?: FavoriteInfo(0, false),
        link = "${apiConfig.siteUrl}/release/${code}.html",
        showDonateDialog = showDonateDialog ?: false,
        blockedInfo = blockedInfo?.toDomain() ?: BlockedInfo(false, null),
        moonwalkLink = moonwalkLink,
        episodes = episodes?.mapNotNull { it.toOnlineDomain(releaseId) }.orEmpty(),
        sourceEpisodes = episodes?.mapNotNull { it.toSourceDomain(releaseId) }.orEmpty(),
        externalPlaylists = externalPlaylists?.map { it.toDomain(releaseId) }.orEmpty(),
        rutubePlaylist = episodes?.mapNotNull { it.toRutubeDomain(releaseId) }.orEmpty(),
        torrents = torrents?.map { it.toDomain(releaseId, apiConfig) }.orEmpty()
    )
}

fun FavoriteInfoResponse.toDomain(): FavoriteInfo = FavoriteInfo(
    rating = rating,
    isAdded = isAdded
)

fun BlockedInfoResponse.toDomain(): BlockedInfo = BlockedInfo(
    isBlocked = isBlocked,
    reason = reason
)

fun MembersResponse.toDomain(): Members = Members(
    timing = timing,
    voicing = voicing,
    editing = editing,
    decorating = decorating,
    translating = translating
)