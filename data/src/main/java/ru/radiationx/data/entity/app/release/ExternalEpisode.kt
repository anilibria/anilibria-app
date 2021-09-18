package ru.radiationx.data.entity.app.release

data class ExternalEpisode(
    val id: Int,
    val releaseId: Int,
    val title: String?,
    val actionTitle: String,
    val service: String?,
    val url: String?
)