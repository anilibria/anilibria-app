package ru.radiationx.data.api.views.mapper

import anilibria.api.views.models.TimeCodeNetwork
import anilibria.api.views.models.ViewHistoryResponse
import ru.radiationx.data.api.releases.mapper.toComboEpisode
import ru.radiationx.data.api.shared.apiDateToDate
import ru.radiationx.data.api.views.models.EpisodeTime
import ru.radiationx.data.api.views.models.TimeCode
import ru.radiationx.data.api.views.models.ViewHistory
import ru.radiationx.data.common.EpisodeUUID
import ru.radiationx.data.common.UserId

fun ViewHistoryResponse.toDomain(): ViewHistory {
    return ViewHistory(
        id = id,
        time = time.toDomainEpisodeTime(),
        userId = UserId(userId),
        isWatched = isWatched,
        updatedAt = updatedAt.apiDateToDate(),
        episodeId = EpisodeUUID(releaseEpisodeId),
        releaseEpisode = releaseEpisode?.toComboEpisode()
    )
}

fun TimeCodeNetwork.toDomain(): TimeCode {
    return TimeCode(
        id = EpisodeUUID(releaseEpisodeId),
        time = time.toDomainEpisodeTime(),
        isWatched = true
    )
}

fun TimeCode.toNetwork(): TimeCodeNetwork {
    return TimeCodeNetwork(
        releaseEpisodeId = id.uuid,
        time = time.toNetwork(),
        isWatched = isWatched
    )
}

fun Double.toDomainEpisodeTime(): EpisodeTime {
    return EpisodeTime((this * 1000).toLong())
}

fun EpisodeTime.toNetwork(): Double {
    return time / 1000.0
}
