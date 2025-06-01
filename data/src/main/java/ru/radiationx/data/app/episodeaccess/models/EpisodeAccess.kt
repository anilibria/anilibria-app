package ru.radiationx.data.app.episodeaccess.models

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.common.EpisodeId

@Parcelize
data class EpisodeAccess(
    val id: EpisodeId,
    val seek: Long,
    val isViewed: Boolean,
    private val lastAccess: Long,
) : Parcelable {

    @IgnoredOnParcel
    val lastAccessRaw = lastAccess

    @IgnoredOnParcel
    val lastValidAccess = lastAccessRaw.takeIf { it > 0L }

    companion object {
        fun createDefault(id: EpisodeId): EpisodeAccess {
            return EpisodeAccess(id, 0L, false, 0L)
        }
    }
}