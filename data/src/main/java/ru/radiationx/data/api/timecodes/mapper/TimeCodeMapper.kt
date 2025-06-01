package ru.radiationx.data.api.timecodes.mapper

import anilibria.api.timecodes.models.TimeCodeNetwork
import ru.radiationx.data.api.timecodes.models.TimeCode
import ru.radiationx.data.common.EpisodeUUID

fun TimeCodeNetwork.toDomain(): TimeCode {
    return TimeCode(
        id = EpisodeUUID(releaseEpisodeId),
        time = (time * 1000).toLong(),
        isWatched = true
    )
}

fun TimeCode.toNetwork(): TimeCodeNetwork {
    return TimeCodeNetwork(
        releaseEpisodeId = id.uuid,
        time = time / 1000.0,
        isWatched = isWatched
    )
}
