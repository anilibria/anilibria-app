package ru.radiationx.data.api.views.models

import ru.radiationx.data.common.EpisodeUUID

data class TimeCode(
    val id: EpisodeUUID,
    val time: EpisodeTime,
    val isWatched: Boolean
)
