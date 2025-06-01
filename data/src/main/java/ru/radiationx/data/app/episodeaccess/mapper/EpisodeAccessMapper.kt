package ru.radiationx.data.app.episodeaccess.mapper

import ru.radiationx.data.app.episodeaccess.db.EpisodeAccessDb
import ru.radiationx.data.app.episodeaccess.db.EpisodeAccessLegacyDb
import ru.radiationx.data.app.episodeaccess.models.EpisodeAccess
import ru.radiationx.data.common.EpisodeId
import ru.radiationx.data.common.ReleaseId

fun EpisodeAccessDb.toDomain(): EpisodeAccess = EpisodeAccess(
    id = EpisodeId(id, ReleaseId(releaseId)),
    seek = seek,
    isViewed = isViewed,
    lastAccess = lastAccess
)

fun EpisodeAccess.toDb(): EpisodeAccessDb = EpisodeAccessDb(
    id = id.id,
    releaseId = id.releaseId.id,
    seek = seek,
    isViewed = isViewed,
    lastAccess = lastAccessRaw
)

fun EpisodeAccessLegacyDb.toDb(): EpisodeAccessDb = EpisodeAccessDb(
    id = id.toString(),
    releaseId = releaseId,
    seek = seek,
    isViewed = isViewed,
    lastAccess = lastAccess
)