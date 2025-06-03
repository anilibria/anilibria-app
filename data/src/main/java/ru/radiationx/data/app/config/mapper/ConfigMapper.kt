package ru.radiationx.data.app.config.mapper

import ru.radiationx.data.app.config.db.ApiAddressDb
import ru.radiationx.data.app.config.db.ApiConfigDataDb
import ru.radiationx.data.app.config.models.ApiAddress
import ru.radiationx.data.app.config.models.ApiAddressId
import ru.radiationx.data.app.config.models.ApiConfigData
import ru.radiationx.data.app.config.remote.ApiAddressResponse
import ru.radiationx.data.app.config.remote.ApiConfigResponse
import ru.radiationx.data.common.toAbsoluteUrl
import ru.radiationx.data.common.toBaseUrl

fun ApiConfigResponse.toDomain(): ApiConfigData = ApiConfigData(
    addresses = addresses.map { it.toDomain() }
)

fun ApiConfigDataDb.toDomain(): ApiConfigData = ApiConfigData(
    addresses = addresses.map { it.toDomain() }
)

fun ApiConfigData.toDb(): ApiConfigDataDb = ApiConfigDataDb(
    addresses = addresses.map { it.toDb() }
)

fun ApiAddressResponse.toDomain(): ApiAddress = ApiAddress(
    id = ApiAddressId(id),
    name = name,
    description = description,
    widget = widget.toBaseUrl(),
    site = site.toBaseUrl(),
    image = image.toBaseUrl(),
    api = api.toBaseUrl(),
    status = status.toAbsoluteUrl()
)

fun ApiAddressDb.toDomain(): ApiAddress = ApiAddress(
    id = ApiAddressId(id),
    name = name,
    description = description,
    widget = widget.toBaseUrl(),
    site = site.toBaseUrl(),
    image = image.toBaseUrl(),
    api = api.toBaseUrl(),
    status = status.toAbsoluteUrl()
)

fun ApiAddress.toDb(): ApiAddressDb = ApiAddressDb(
    id = id.id,
    name = name,
    description = description,
    widget = widget.value,
    site = site.value,
    image = image.value,
    api = api.value,
    status = status.value
)

