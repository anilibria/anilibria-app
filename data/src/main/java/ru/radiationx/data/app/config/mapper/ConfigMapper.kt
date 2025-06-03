package ru.radiationx.data.app.config.mapper

import ru.radiationx.data.app.config.db.AppConfigAddressDb
import ru.radiationx.data.app.config.db.AppConfigDataDb
import ru.radiationx.data.app.config.models.AppConfigAddress
import ru.radiationx.data.app.config.models.AppConfigAddressId
import ru.radiationx.data.app.config.models.AppConfigData
import ru.radiationx.data.app.config.remote.AppConfigAddressResponse
import ru.radiationx.data.app.config.remote.AppConfigResponse
import ru.radiationx.data.common.toAbsoluteUrl
import ru.radiationx.data.common.toBaseUrl

fun AppConfigResponse.toDomain(): AppConfigData = AppConfigData(
    addresses = addresses.map { it.toDomain() }
)

fun AppConfigDataDb.toDomain(): AppConfigData = AppConfigData(
    addresses = addresses.map { it.toDomain() }
)

fun AppConfigData.toDb(): AppConfigDataDb = AppConfigDataDb(
    addresses = addresses.map { it.toDb() }
)

fun AppConfigAddressResponse.toDomain(): AppConfigAddress = AppConfigAddress(
    id = AppConfigAddressId(id),
    name = name,
    description = description,
    widget = widget.toBaseUrl(),
    site = site.toBaseUrl(),
    image = image.toBaseUrl(),
    api = api.toBaseUrl(),
    status = status.toAbsoluteUrl()
)

fun AppConfigAddressDb.toDomain(): AppConfigAddress = AppConfigAddress(
    id = AppConfigAddressId(id),
    name = name,
    description = description,
    widget = widget.toBaseUrl(),
    site = site.toBaseUrl(),
    image = image.toBaseUrl(),
    api = api.toBaseUrl(),
    status = status.toAbsoluteUrl()
)

fun AppConfigAddress.toDb(): AppConfigAddressDb = AppConfigAddressDb(
    id = id.id,
    name = name,
    description = description,
    widget = widget.value,
    site = site.value,
    image = image.value,
    api = api.value,
    status = status.value
)

