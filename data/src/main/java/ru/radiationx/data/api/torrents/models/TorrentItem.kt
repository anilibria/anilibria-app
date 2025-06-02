package ru.radiationx.data.api.torrents.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.common.TorrentId
import ru.radiationx.data.common.Url
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

    val url: Url.Path
        get() = Url.pathOf("/api/v1/anime/torrents/${id.id}/file")
}