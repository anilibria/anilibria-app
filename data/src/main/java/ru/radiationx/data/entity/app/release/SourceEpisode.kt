package ru.radiationx.data.entity.app.release

data class SourceEpisode(
    val id: Int,
    val releaseId: Int,
    val title: String?,
    val urlSd: String?,
    val urlHd: String?,
    val urlFullHd: String?
)