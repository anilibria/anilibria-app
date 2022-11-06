package ru.radiationx.data.entity.mapper

import android.util.Log
import ru.radiationx.data.entity.domain.release.*
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.response.release.EpisodeResponse
import ru.radiationx.data.entity.response.release.ExternalEpisodeResponse
import ru.radiationx.data.entity.response.release.ExternalPlaylistResponse
import ru.radiationx.data.entity.response.release.PlayerSkipsResponse
import java.math.BigDecimal

// placeholder for moment when src downloading disabled
private const val VK_URL = "https://vk.com/anilibria?w=wall-37468416_493445"

// episode ids can be float/double/int e.g. 25, 25.5.
private fun Float.toId(releaseId: ReleaseId): EpisodeId {
    val big = BigDecimal(toDouble())
    return EpisodeId(big.toString(), releaseId).also {
        Log.d("kekeke", "toId $this; ${big} -> $it")
    }
}

fun EpisodeResponse.toOnlineDomain(releaseId: ReleaseId): Episode? {
    if (sources?.isAnilibria != true) {
        return null
    }
    val episodeId = id.toId(releaseId)
    return Episode(
        id = episodeId,
        title = title,
        urlSd = urlSd,
        urlHd = urlHd,
        urlFullHd = urlFullHd,
        updatedAt = updatedAt?.secToDate(),
        skips = skips?.toDomain(),
        access = EpisodeAccess(
            id = episodeId,
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

private fun List<Int>.toSkipDomain(): PlayerSkips.Skip? {
    val start = getOrNull(0)?.secToMillis() ?: return null
    val end = getOrNull(0)?.secToMillis() ?: return null
    return PlayerSkips.Skip(
        start = start,
        end = end
    )
}


fun EpisodeResponse.toSourceDomain(releaseId: ReleaseId): SourceEpisode? {
    if (sources?.isAnilibria != true) {
        return null
    }
    return SourceEpisode(
        id = id.toId(releaseId),
        title = title,
        updatedAt = updatedAt?.secToDate(),
        urlSd = srcUrlSd?.takeIf { it != VK_URL },
        urlHd = srcUrlHd?.takeIf { it != VK_URL },
        urlFullHd = srcUrlFullHd?.takeIf { it != VK_URL }
    )
}

fun EpisodeResponse.toRutubeDomain(releaseId: ReleaseId): RutubeEpisode? {
    if (sources?.isRutube != true || rutubeId == null) {
        return null
    }
    return RutubeEpisode(
        id = id.toId(releaseId),
        title = title,
        updatedAt = updatedAt?.secToDate(),
        rutubeId = rutubeId,
        url = "https://rutube.ru/play/embed/$rutubeId"
    )
}

fun ExternalPlaylistResponse.toDomain(releaseId: ReleaseId): ExternalPlaylist {
    return ExternalPlaylist(
        tag,
        title,
        actionText,
        episodes.map { it.toDomain(releaseId) }
    )
}

fun ExternalEpisodeResponse.toDomain(releaseId: ReleaseId): ExternalEpisode = ExternalEpisode(
    id = id.toId(releaseId),
    title = title,
    url = url
)