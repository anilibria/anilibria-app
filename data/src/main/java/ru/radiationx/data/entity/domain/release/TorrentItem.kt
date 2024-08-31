package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.domain.types.TorrentId
import java.util.Date

@Parcelize
data class TorrentItem(
    val id: TorrentId,
    val hash: String?,
    val leechers: Int,
    val seeders: Int,
    val completed: Int,
    val quality: String?,
    // todo API2 use this
    val codec: String?,
    // todo API2 use this
    val color: String?,
    val series: String?,
    val size: Long,
    // todo API2 update usage
    //val url: String?,
    val date: Date?
) : Parcelable