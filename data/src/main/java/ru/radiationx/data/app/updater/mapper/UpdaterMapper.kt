package ru.radiationx.data.app.updater.mapper

import ru.radiationx.data.app.updater.models.UpdateData
import ru.radiationx.data.app.updater.remote.UpdateDataResponse
import ru.radiationx.data.common.toAbsoluteUrl

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
