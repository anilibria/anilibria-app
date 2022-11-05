package ru.radiationx.data.entity.response.release

import java.io.Serializable
import java.util.*

data class SourceEpisode(
    val id: Int,
    val releaseId: Int,
    val title: String?,
    val updatedAt: Date?,
    val urlSd: String?,
    val urlHd: String?,
    val urlFullHd: String?
) : Serializable