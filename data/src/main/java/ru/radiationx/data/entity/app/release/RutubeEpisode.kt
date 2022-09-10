package ru.radiationx.data.entity.app.release

import java.io.Serializable

data class RutubeEpisode(
    val id: Int,
    val releaseId: Int,
    val title: String?,
    val rutubeId: String,
    val url: String
) : Serializable