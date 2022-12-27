package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.domain.other.LinkMenuItem
import ru.radiationx.data.entity.response.other.LinkMenuResponse

fun LinkMenuResponse.toDomain(): LinkMenuItem = LinkMenuItem(
    title = title,
    absoluteLink = absoluteLink,
    sitePagePath = sitePagePath,
    icon = icon
)