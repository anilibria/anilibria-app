package ru.radiationx.data.api.torrents

import anilibria.api.torrent.TorrentsApi
import okhttp3.ResponseBody
import ru.radiationx.data.api.shared.pagination.Paginated
import ru.radiationx.data.api.shared.pagination.toDomain
import ru.radiationx.data.api.torrents.mapper.toDomain
import ru.radiationx.data.api.torrents.models.TorrentItem
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.data.common.TorrentId
import toothpick.InjectConstructor

@InjectConstructor
class TorrentsApiDataSource(
    private val api: TorrentsApi
) {

    suspend fun getTorrents(releaseId: ReleaseId, page: Int?, limit: Int?): Paginated<TorrentItem> {
        return api
            .getTorrents(page, limit)
            .toDomain { it.toDomain(releaseId) }
    }

    suspend fun getTorrent(torrentId: TorrentId): TorrentItem {
        return api
            .getTorrent(torrentId.id.toString())
            .toDomain(torrentId.releaseId)
    }

    suspend fun getTorrentFile(torrentId: TorrentId): ResponseBody {
        return api.getTorrentFile(torrentId.id.toString(), null)
    }

    suspend fun getTorrentsByRelease(releaseId: ReleaseId): List<TorrentItem> {
        return api
            .getTorrentsByRelease(releaseId.id)
            .map { it.toDomain(releaseId) }
    }
}