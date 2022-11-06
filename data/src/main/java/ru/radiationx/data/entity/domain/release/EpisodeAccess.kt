package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.radiationx.data.entity.domain.types.EpisodeId

@Parcelize
data class EpisodeAccess(
    val id: EpisodeId,
    val seek: Long,
    val isViewed: Boolean,
    val lastAccess: Long,
) : Parcelable