package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.domain.types.EpisodeId

@Parcelize
data class ExternalEpisode(
    val id: EpisodeId,
    val title: String?,
    val url: String?
) : Parcelable