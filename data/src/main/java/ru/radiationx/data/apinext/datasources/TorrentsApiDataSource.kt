package ru.radiationx.data.apinext.datasources

import anilibria.api.torrent.TorrentsApi
import okhttp3.ResponseBody
import ru.radiationx.data.apinext.toDomain
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.release.TorrentItem
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.domain.types.TorrentId
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