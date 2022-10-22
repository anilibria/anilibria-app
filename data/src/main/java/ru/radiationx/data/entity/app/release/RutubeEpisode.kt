package ru.radiationx.data.entity.app.release

import java.io.Serializable
import java.util.*

data class RutubeEpisode(
    val id: Int,
    val releaseId: Int,
    val title: String?,
    val updatedAt: Date?,
    val rutubeId: String,
    val url: String
) : Serializable