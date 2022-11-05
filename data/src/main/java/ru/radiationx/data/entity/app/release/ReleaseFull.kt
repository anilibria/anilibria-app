package ru.radiationx.data.entity.app.release

import java.io.Serializable

data class ReleaseFull(
    val item: ReleaseItem,
    val showDonateDialog: Boolean,
    val blockedInfo: BlockedInfo,
    val moonwalkLink: String?,
    val episodes: List<Episode>,
    val sourceEpisodes: List<SourceEpisode>,
    val externalPlaylists: List<ExternalPlaylist>,
    val rutubePlaylist: List<RutubeEpisode>,
    val torrents: List<TorrentItem>
) : ReleaseItem(
    id = item.id,
    code = item.code,
    names = item.names,
    series = item.series,
    poster = item.poster,
    torrentUpdate = item.torrentUpdate,
    status = item.status,
    statusCode = item.statusCode,
    types = item.types,
    genres = item.genres,
    voices = item.voices,
    seasons = item.seasons,
    days = item.days,
    description = item.description,
    announce = item.announce,
    favoriteInfo = item.favoriteInfo,
    link = item.link
), Serializable {

    companion object {

        fun emptyBy(item: ReleaseItem): ReleaseFull = ReleaseFull(
            item = item,
            showDonateDialog = false,
            blockedInfo = BlockedInfo(isBlocked = false, reason = null),
            moonwalkLink = null,
            episodes = listOf(),
            sourceEpisodes = listOf(),
            externalPlaylists = listOf(),
            rutubePlaylist = listOf(),
            torrents = listOf(),
        )
    }

}
