package ru.radiationx.data.entity.response.release

import java.io.Serializable
import java.util.*

data class RutubeEpisodeResponse(
    val id: Int,
    val releaseId: Int,
    val title: String?,
    val updatedAt: Date?,
    val rutubeId: String,
    val url: String
) : Serializable