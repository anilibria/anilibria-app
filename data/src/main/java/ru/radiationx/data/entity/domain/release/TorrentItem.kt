package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.common.Url
import ru.radiationx.data.entity.domain.types.TorrentId
import java.util.Date

@Parcelize
data class TorrentItem(
    val id: TorrentId,
    val hash: String?,
    val leechers: Int,
    val seeders: Int,
    val completed: Int,
    val type: String?,
    val quality: String?,
    val codec: String?,
    val color: String?,
    val series: String?,
    val size: Long,
    val date: Date?,
    val magnet: String
) : Parcelable {

    val url: Url.Relative
        get() = Url.relativeOf("/api/v1/anime/torrents/${id.id}/file")
}