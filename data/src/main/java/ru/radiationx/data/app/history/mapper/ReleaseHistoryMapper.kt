package ru.radiationx.data.app.history.mapper

import ru.radiationx.data.app.history.db.ReleaseHistoryDb
import ru.radiationx.data.common.ReleaseId


fun ReleaseId.toHistoryDb(): ReleaseHistoryDb {
    return ReleaseHistoryDb(id = id)
}

fun ReleaseHistoryDb.toDomain(): ReleaseId {
    return ReleaseId(id = id)
}