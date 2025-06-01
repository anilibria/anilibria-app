package ru.radiationx.anilibria.screen.player

import ru.radiationx.data.api.releases.models.PlayerSkips

data class Video(
    val url: String,
    val seek: Long,
    val title: String,
    val subtitle: String,
    val skips: PlayerSkips?,
)