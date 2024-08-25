package ru.radiationx.data.historyfile.mapper

import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.release.ReleaseUpdate
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.mapper.dateToSec
import ru.radiationx.data.entity.mapper.secToDate
import ru.radiationx.data.historyfile.models.EpisodeAccessExport
import ru.radiationx.data.historyfile.models.ReleaseHistoryExport
import ru.radiationx.data.historyfile.models.ReleaseUpdateExport

fun EpisodeAccessExport.toDomain() = EpisodeAccess(
    id = EpisodeId(id, ReleaseId(releaseId)),
    seek = seek,
    isViewed = isViewed,
    lastAccess = lastAccess
)

fun ReleaseUpdateExport.toDomain() = ReleaseUpdate(
    id = ReleaseId(id),
    timestamp = timestamp.secToDate(),
    lastOpenTimestamp = lastOpenTimestamp.secToDate()
)

fun ReleaseHistoryExport.toDomain() = ReleaseId(id)

fun EpisodeAccess.toExport() = EpisodeAccessExport(
    id = id.id,
    releaseId = id.releaseId.id,
    seek = seek,
    isViewed = isViewed,
    lastAccess = lastAccessRaw
)

fun ReleaseUpdate.toExport() = ReleaseUpdateExport(
    id = id.id,
    timestamp = timestamp.dateToSec(),
    lastOpenTimestamp = lastOpenTimestamp.dateToSec()
)

fun ReleaseId.toExport() = ReleaseHistoryExport(
    id = id
)