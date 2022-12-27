package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.domain.page.VkComments
import ru.radiationx.data.entity.response.page.VkCommentsResponse
import java.util.*

fun VkCommentsResponse.toDomain() = VkComments(
    baseUrl = baseUrl,
    script = script
)

fun String.appendBaseUrl(baseUrl: String): String {
    return "$baseUrl$this"
}

fun Int.secToDate(): Date = Date(secToMillis())

fun Int.secToMillis(): Long = this * 1000L