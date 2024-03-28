package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.domain.types.EpisodeId
import java.util.Date

@Parcelize
data class Episode(
    val id: EpisodeId,
    val title: String?,
    val qualityInfo: QualityInfo,
    val updatedAt: Date?,
    val skips: PlayerSkips?,
) : Parcelable