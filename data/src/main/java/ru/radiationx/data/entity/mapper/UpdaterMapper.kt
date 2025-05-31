package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.common.toAbsoluteUrl
import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.data.entity.response.updater.UpdateDataResponse

fun UpdateDataResponse.toDomain(currentCode: Int): UpdateData {
    val updateCode = code.toIntOrNull() ?: 0
    return UpdateData(
        hasUpdate = updateCode > currentCode,
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
}

fun UpdateDataResponse.UpdateLink.toDomain() = UpdateData.UpdateLink(
    name = name,
    url = url.toAbsoluteUrl(),
    type = when (type) {
        "file" -> UpdateData.LinkType.FILE
        "site" -> UpdateData.LinkType.SITE
        else -> UpdateData.LinkType.SITE
    }
)
