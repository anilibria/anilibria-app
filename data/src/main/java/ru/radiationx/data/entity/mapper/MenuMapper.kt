package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.common.toAbsoluteUrl
import ru.radiationx.data.entity.common.toRelativeUrl
import ru.radiationx.data.entity.domain.other.LinkMenuItem
import ru.radiationx.data.entity.response.other.LinkMenuResponse

fun LinkMenuResponse.toDomain(): LinkMenuItem = LinkMenuItem(
    title = title,
    link = absoluteLink?.toAbsoluteUrl(),
    pagePath = sitePagePath?.toRelativeUrl(),
    icon = icon
)