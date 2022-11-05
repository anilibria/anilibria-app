package ru.radiationx.data.entity.response.release

import java.io.Serializable

data class ExternalEpisodeResponse(
    val id: Int,
    val releaseId: Int,
    val title: String?,
    val url: String?
) : Serializable