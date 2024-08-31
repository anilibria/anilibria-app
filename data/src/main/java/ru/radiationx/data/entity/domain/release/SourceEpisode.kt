package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.domain.types.EpisodeId
import java.util.Date

// todo API2 delete
@Parcelize
data class SourceEpisode(
    val id: EpisodeId,
    val title: String?,
    val updatedAt: Date?,
    val qualityInfo: QualityInfo,
) : Parcelable