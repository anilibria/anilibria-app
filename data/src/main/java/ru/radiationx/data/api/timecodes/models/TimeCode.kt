package ru.radiationx.data.api.timecodes.models

import ru.radiationx.data.common.EpisodeUUID

data class TimeCode(
    val id: EpisodeUUID,
    val time: Long,
    val isWatched: Boolean
)
