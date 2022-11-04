package ru.radiationx.data.entity.app.release

import java.io.Serializable
import java.util.*

data class Episode(
    val releaseId: Int,
    val id: Int,
    val title: String?,
    val urlSd: String?,
    val urlHd: String?,
    val urlFullHd: String?,
    val updatedAt: Date?,
    val skips: PlayerSkips?,
    val access: EpisodeAccess
) : Serializable