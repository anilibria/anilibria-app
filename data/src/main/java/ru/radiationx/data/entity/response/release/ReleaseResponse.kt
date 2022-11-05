package ru.radiationx.data.entity.response.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class ReleaseResponse(
    // base
    @Json(name = "id") val id: Int,
    @Json(name = "code") val code: String,
    @Json(name = "names") val names: List<String>?,
    @Json(name = "series") val series: String?,
    @Json(name = "poster") val poster: String?,
    @Json(name = "last") val torrentUpdate: Int?,
    @Json(name = "status") val status: String?,
    @Json(name = "statusCode") val statusCode: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "genres") val genres: List<String>?,
    @Json(name = "voices") val voices: List<String>?,
    @Json(name = "year") val season: String?,
    @Json(name = "day") val day: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "announce") val announce: String?,
    @Json(name = "favorite") val favorite: FavoriteInfoResponse,

    // full
    @Json(name = "showDonateDialog") val showDonateDialog: Boolean,
    @Json(name = "blockedInfo") val blockedInfo: BlockedInfoResponse,
    @Json(name = "moon") val moonwalkLink: String?,
    @Json(name = "playlist") val episodes: List<EpisodeResponse>,
    @Json(name = "externalPlaylist") val externalPlaylists: List<ExternalPlaylistResponse>,
    @Json(name = "torrents") val torrents: List<TorrentResponse>
) : Serializable
