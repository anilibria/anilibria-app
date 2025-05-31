package ru.radiationx.data.apinext.models

import ru.radiationx.data.entity.domain.types.EpisodeUUID

data class TimeCode(
    val id: EpisodeUUID,
    val time: Long,
    val isWatched: Boolean
)
