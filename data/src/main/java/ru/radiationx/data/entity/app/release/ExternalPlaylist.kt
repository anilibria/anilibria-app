package ru.radiationx.data.entity.app.release

import java.io.Serializable

data class ExternalPlaylist(
    val tag: String,
    val title: String,
    val actionText: String,
    val episodes: List<ExternalEpisode>
) : Serializable