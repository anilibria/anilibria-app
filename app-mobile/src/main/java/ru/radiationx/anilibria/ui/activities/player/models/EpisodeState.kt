package ru.radiationx.anilibria.ui.activities.player.models

import ru.radiationx.data.api.releases.models.PlayerSkips
import ru.radiationx.data.common.EpisodeId

data class EpisodeState(
    val id: EpisodeId,
    val title: String,
    val url: String,
    val skips: PlayerSkips?,
)