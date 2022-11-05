package ru.radiationx.data.entity.app.release

import java.io.Serializable

data class EpisodeAccess(
    val releaseId: Int,
    val id: Int,
    val seek: Long,
    val isViewed: Boolean,
    val lastAccess: Long,
) : Serializable