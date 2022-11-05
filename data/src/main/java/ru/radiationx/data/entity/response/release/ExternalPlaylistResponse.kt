package ru.radiationx.data.entity.response.release

import java.io.Serializable

data class ExternalPlaylistResponse(
    val tag: String,
    val title: String,
    val actionText: String,
    val episodes: List<ExternalEpisodeResponse>
) : Serializable