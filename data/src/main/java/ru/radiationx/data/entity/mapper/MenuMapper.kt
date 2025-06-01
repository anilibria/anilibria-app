package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.common.toAbsoluteUrl
import ru.radiationx.data.entity.common.toRelativeUrl
import ru.radiationx.data.entity.db.LinkMenuDb
import ru.radiationx.data.entity.domain.other.LinkMenuItem
import ru.radiationx.data.entity.response.other.LinkMenuResponse

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