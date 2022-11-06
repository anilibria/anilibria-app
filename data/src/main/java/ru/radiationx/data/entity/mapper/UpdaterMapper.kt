package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.data.entity.response.updater.UpdateDataResponse

fun UpdateDataResponse.toDomain() = UpdateData(
    code = code.toIntOrNull() ?: 0,
    build = build.toIntOrNull() ?: 0,
    name = name,
    date = date,
    links = links.map { it.toDomain() },
    important = important,
    added = added,
    fixed = fixed,
    changed = changed
)

fun UpdateDataResponse.UpdateLink.toDomain() = UpdateData.UpdateLink(
    name = name ?: "Unknown",
    url = url.orEmpty(),
    type = type ?: "site"
)