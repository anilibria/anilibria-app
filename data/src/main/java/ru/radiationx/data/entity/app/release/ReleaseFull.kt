package ru.radiationx.data.entity.app.release

import java.io.Serializable

class ReleaseFull(
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
    isNew = item.isNew,
    link = item.link
), Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReleaseFull) return false
        if (!super.equals(other)) return false

        if (showDonateDialog != other.showDonateDialog) return false
        if (blockedInfo != other.blockedInfo) return false
        if (moonwalkLink != other.moonwalkLink) return false
        if (episodes != other.episodes) return false
        if (sourceEpisodes != other.sourceEpisodes) return false
        if (torrents != other.torrents) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + showDonateDialog.hashCode()
        result = 31 * result + blockedInfo.hashCode()
        result = 31 * result + (moonwalkLink?.hashCode() ?: 0)
        result = 31 * result + episodes.hashCode()
        result = 31 * result + sourceEpisodes.hashCode()
        result = 31 * result + torrents.hashCode()
        return result
    }


}
