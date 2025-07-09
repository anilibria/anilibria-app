package ru.radiationx.data.api.status.mapper

import anilibria.api.status.models.StatusResponse
import ru.radiationx.data.api.status.model.Status

fun StatusResponse.toDomain(): Status {
    return Status(
        request = request?.toDomain(),
        isAlive = isAlive,
        availableApiEndpoints = availableApiEndpoints
    )
}

fun StatusResponse.Request.toDomain(): Status.Request {
    return Status.Request(
        ip = ip,
        country = country,
        isoCode = isoCode,
        timeZone = timeZone
    )
}