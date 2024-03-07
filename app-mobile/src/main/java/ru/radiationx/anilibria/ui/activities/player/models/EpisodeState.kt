package ru.radiationx.anilibria.ui.activities.player.models

import ru.radiationx.data.entity.domain.release.PlayerSkips
import ru.radiationx.data.entity.domain.types.EpisodeId

data class EpisodeState(
    val id: EpisodeId,
    val title: String,
    val url: String,
    val skips: PlayerSkips?,
)