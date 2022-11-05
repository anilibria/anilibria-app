package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.app.release.*
import ru.radiationx.data.entity.response.release.EpisodeResponse
import ru.radiationx.data.entity.response.release.ExternalEpisodeResponse
import ru.radiationx.data.entity.response.release.ExternalPlaylistResponse
import ru.radiationx.data.entity.response.release.PlayerSkipsResponse

// placeholder for moment when src downloading disabled
private const val VK_URL = "https://vk.com/anilibria?w=wall-37468416_493445"

fun EpisodeResponse.toOnlineDomain(releaseId: Int): Episode? {
    if (sources?.isAnilibria != true) {
        return null
    }
    return Episode(
        releaseId = releaseId,
        id = id,
        title = title,
        urlSd = urlSd,
        urlHd = urlHd,
        urlFullHd = urlFullHd,
        updatedAt = updatedAt?.secToDate(),
        skips = skips?.toDomain(),
        access = EpisodeAccess(
            releaseId = releaseId,
            id = id,
            seek = 0,
            isViewed = false,
            lastAccess = 0
        )
    )
}

fun PlayerSkipsResponse.toDomain(): PlayerSkips = PlayerSkips(
    opening = opening?.toSkipDomain(),
    ending = ending?.toSkipDomain()
)

private fun List<Int>.toSkipDomain(): PlayerSkips.Skip = PlayerSkips.Skip(
    start = get(0).secToMillis(),
    end = get(1).secToMillis()
)


fun EpisodeResponse.toSourceDomain(releaseId: Int): SourceEpisode? {
    if (sources?.isAnilibria != true) {
        return null
    }
    return SourceEpisode(
        id = id,
        releaseId = releaseId,
        title = title,
        updatedAt = updatedAt?.secToDate(),
        urlSd = srcUrlSd?.takeIf { it != VK_URL },
        urlHd = srcUrlHd?.takeIf { it != VK_URL },
        urlFullHd = srcUrlFullHd?.takeIf { it != VK_URL }
    )
}

fun EpisodeResponse.toRutubeDomain(releaseId: Int): RutubeEpisode? {
    if (sources?.isRutube != true || rutubeId == null) {
        return null
    }
    return RutubeEpisode(
        id = id,
        releaseId = releaseId,
        title = title,
        updatedAt = updatedAt?.secToDate(),
        rutubeId = rutubeId,
        url = "https://rutube.ru/play/embed/$rutubeId"
    )
}

fun ExternalPlaylistResponse.toDomain(releaseId: Int): ExternalPlaylist {
    return ExternalPlaylist(
        tag,
        title,
        actionText,
        episodes.map { it.toDomain(releaseId) }
    )
}

fun ExternalEpisodeResponse.toDomain(releaseId: Int): ExternalEpisode = ExternalEpisode(
    id = id,
    releaseId = releaseId,
    title = title,
    url = url
)