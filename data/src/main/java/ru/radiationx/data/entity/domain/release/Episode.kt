package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.domain.types.EpisodeId
import java.util.*

@Parcelize
data class Episode(
    val id: EpisodeId,
    val title: String?,
    val urlSd: String?,
    val urlHd: String?,
    val urlFullHd: String?,
    val updatedAt: Date?,
    val skips: PlayerSkips?,
    val access: EpisodeAccess
) : Parcelable