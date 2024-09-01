package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.domain.types.EpisodeId
import java.util.Date

@Parcelize
data class RutubeEpisode(
    val id: EpisodeId,
    val title: String?,
    val updatedAt: Date?,
    val rutubeId: String,
    val url: String
) : Parcelable