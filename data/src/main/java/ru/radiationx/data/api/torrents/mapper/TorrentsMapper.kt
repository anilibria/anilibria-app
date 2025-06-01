package ru.radiationx.data.api.torrents.mapper

import anilibria.api.torrent.models.TorrentResponse
import ru.radiationx.data.api.shared.apiDateToDate
import ru.radiationx.data.api.torrents.models.TorrentItem
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.data.common.TorrentId


fun TorrentResponse.toDomain(releaseId: ReleaseId): TorrentItem {
    return TorrentItem(
        id = TorrentId(
            id = id,
            releaseId = releaseId
        ),
        hash = hash,
        leechers = leechers,
        seeders = seeders,
        completed = completedTimes,
        type = type.description,
        quality = quality.description,
        codec = codec.description,
        color = color.description,
        series = description,
        size = size,
        date = updatedAt.apiDateToDate(),
        magnet = magnet
    )
}