package ru.radiationx.data.app.menu.mapper

import ru.radiationx.data.app.menu.db.LinkMenuDb
import ru.radiationx.data.app.menu.models.LinkMenuItem
import ru.radiationx.data.app.menu.remote.LinkMenuResponse
import ru.radiationx.data.common.toAbsoluteUrl
import ru.radiationx.data.common.toRelativeUrl

fun LinkMenuResponse.toDomain(): LinkMenuItem = LinkMenuItem(
    title = title,
    link = link?.toAbsoluteUrl(),
    pagePath = pagePath?.toRelativeUrl(),
    icon = icon
)

fun LinkMenuItem.toDb(): LinkMenuDb {
    return LinkMenuDb(
        title = title,
        link = link?.raw,
        pagePath = pagePath?.raw,
        icon = icon
    )
}

fun LinkMenuDb.toDomain(): LinkMenuItem {
    return LinkMenuItem(
        title = title,
        link = link?.toAbsoluteUrl(),
        pagePath = pagePath?.toRelativeUrl(),
        icon = icon
    )
}