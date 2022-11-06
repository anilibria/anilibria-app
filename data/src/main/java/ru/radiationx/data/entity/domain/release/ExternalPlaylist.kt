package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ExternalPlaylist(
    val tag: String,
    val title: String,
    val actionText: String,
    val episodes: List<ExternalEpisode>
) : Parcelable