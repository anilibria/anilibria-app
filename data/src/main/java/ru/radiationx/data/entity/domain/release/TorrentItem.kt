package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.domain.types.TorrentId
import java.util.*

@Parcelize
data class TorrentItem(
    val id: TorrentId,
    val hash: String?,
    val leechers: Int,
    val seeders: Int,
    val completed: Int,
    val quality: String?,
    val series: String?,
    val size: Long,
    val url: String?,
    val date: Date?
) : Parcelable