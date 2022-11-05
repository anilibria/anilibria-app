package ru.radiationx.data.entity.app.release

import java.io.Serializable

/* Created by radiationx on 31.10.17. */

data class Release(
    // base
    val id: Int,
    val code: String?,
    val names: List<String>,
    val series: String?,
    val poster: String?,
    val torrentUpdate: Int,
    val status: String?,
    val statusCode: String?,
    val types: List<String>,
    val genres: List<String>,
    val voices: List<String>,
    val seasons: List<String>,
    val days: List<String>,
    val description: String?,
    val announce: String?,
    val favoriteInfo: FavoriteInfo,
    val link: String?,

    // full
    val showDonateDialog: Boolean,
    val blockedInfo: BlockedInfo,
    val moonwalkLink: String?,
    val episodes: List<Episode>,
    val sourceEpisodes: List<SourceEpisode>,
    val externalPlaylists: List<ExternalPlaylist>,
    val rutubePlaylist: List<RutubeEpisode>,
    val torrents: List<TorrentItem>
) : Serializable {


    companion object {
        const val STATUS_CODE_PROGRESS = "1"
        const val STATUS_CODE_COMPLETE = "2"
        const val STATUS_CODE_HIDDEN = "3"
        const val STATUS_CODE_NOT_ONGOING = "4"
    }

    val title: String?
        get() = names.firstOrNull()

    val titleEng: String?
        get() = names.lastOrNull()
}
