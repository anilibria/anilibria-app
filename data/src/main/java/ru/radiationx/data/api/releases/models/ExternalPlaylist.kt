package ru.radiationx.data.api.releases.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalPlaylist(
    val tag: String,
    val title: String,
    val actionText: String,
    val episodes: List<ExternalEpisode>
) : Parcelable