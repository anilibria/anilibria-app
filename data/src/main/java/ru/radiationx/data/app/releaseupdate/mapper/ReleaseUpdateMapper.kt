package ru.radiationx.data.app.releaseupdate.mapper

import ru.radiationx.data.api.shared.dateToSec
import ru.radiationx.data.api.shared.secToDate
import ru.radiationx.data.app.releaseupdate.db.ReleaseUpdateDb
import ru.radiationx.data.app.releaseupdate.models.ReleaseUpdate
import ru.radiationx.data.common.ReleaseId

fun ReleaseUpdateDb.toDomain() = ReleaseUpdate(
    id = ReleaseId(id),
    timestamp = timestamp.secToDate(),
    lastOpenTimestamp = lastOpenTimestamp.secToDate()
)

fun ReleaseUpdate.toDb() = ReleaseUpdateDb(
    id = id.id,
    timestamp = timestamp.dateToSec(),
    lastOpenTimestamp = lastOpenTimestamp.dateToSec()
)