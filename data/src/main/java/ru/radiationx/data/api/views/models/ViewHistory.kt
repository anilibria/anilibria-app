package ru.radiationx.data.api.views.models

import ru.radiationx.data.api.releases.models.ComboEpisode
import ru.radiationx.data.api.releases.models.Episode
import ru.radiationx.data.common.EpisodeUUID
import ru.radiationx.data.common.UserId
import java.util.Date

data class ViewHistory(
    val id: Long,
    val time: EpisodeTime,
    val userId: UserId,
    val isWatched: Boolean,
    val updatedAt: Date,
    val episodeId: EpisodeUUID,
    val releaseEpisode: ComboEpisode?,
)
