package ru.radiationx.data.entity.response.release

import java.io.Serializable
import java.util.*

data class EpisodeResponse(
    val releaseId: Int,
    val id: Int,
    val title: String?,
    val urlSd: String?,
    val urlHd: String?,
    val urlFullHd: String?,
    val updatedAt: Date?,
    val skips: PlayerSkipsResponse?,
) : Serializable