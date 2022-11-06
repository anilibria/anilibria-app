package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.db.EpisodeAccessDb
import ru.radiationx.data.entity.db.EpisodeAccessLegacyDb
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId

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
    lastAccess = lastAccess
)

fun EpisodeAccessLegacyDb.toDb(): EpisodeAccessDb = EpisodeAccessDb(
    id = id.toString(),
    releaseId = releaseId,
    seek = seek,
    isViewed = isViewed,
    lastAccess = lastAccess
)