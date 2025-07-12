package ru.radiationx.data.api.releases.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.common.EpisodeId
import ru.radiationx.data.common.EpisodeUUID
import java.util.Date

@Parcelize
data class Episode(
    val uuid: EpisodeUUID,
    val id: EpisodeId,
    val title: String?,
    val qualityInfo: QualityInfo,
    val updatedAt: Date?,
    val skips: PlayerSkips?,
) : Parcelable