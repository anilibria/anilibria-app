package ru.radiationx.data.app.historyfile.mapper

import ru.radiationx.data.api.shared.dateToSec
import ru.radiationx.data.api.shared.secToDate
import ru.radiationx.data.app.episodeaccess.models.EpisodeAccess
import ru.radiationx.data.app.historyfile.models.EpisodeAccessExport
import ru.radiationx.data.app.historyfile.models.ReleaseHistoryExport
import ru.radiationx.data.app.historyfile.models.ReleaseUpdateExport
import ru.radiationx.data.app.releaseupdate.models.ReleaseUpdate
import ru.radiationx.data.common.EpisodeId
import ru.radiationx.data.common.ReleaseId

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