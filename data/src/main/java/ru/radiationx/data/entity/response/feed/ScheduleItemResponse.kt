package ru.radiationx.data.entity.response.feed

import ru.radiationx.data.entity.response.release.ReleaseResponse

data class ScheduleItemResponse(
    val releaseItem: ReleaseResponse,
    val completed: Boolean
)