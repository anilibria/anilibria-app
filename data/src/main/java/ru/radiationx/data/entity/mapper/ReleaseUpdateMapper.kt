package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.db.ReleaseUpdateDb
import ru.radiationx.data.entity.domain.release.ReleaseUpdate
import ru.radiationx.data.entity.domain.types.ReleaseId

fun ReleaseUpdateDb.toDomain() = ReleaseUpdate(
    id = ReleaseId(id),
    timestamp = timestamp,
    lastOpenTimestamp = lastOpenTimestamp
)

fun ReleaseUpdate.toDb() = ReleaseUpdateDb(
    id = id.id,
    timestamp = timestamp,
    lastOpenTimestamp = lastOpenTimestamp
)