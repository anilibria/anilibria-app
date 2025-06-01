package ru.radiationx.data.app.config.mapper

import ru.radiationx.data.app.config.models.ApiAddress
import ru.radiationx.data.app.config.models.ApiConfigData
import ru.radiationx.data.app.config.models.ApiProxy
import ru.radiationx.data.app.config.remote.ApiConfigAddressResponse
import ru.radiationx.data.app.config.remote.ApiConfigProxyResponse
import ru.radiationx.data.app.config.remote.ApiConfigResponse

fun ApiConfigResponse.toDomain(): ApiConfigData = ApiConfigData(
    addresses.map { it.toDomain() }
)

fun ApiConfigAddressResponse.toDomain(): ApiAddress = ApiAddress(
    tag = tag,
    name = name,
    desc = desc,
    widgetsSite = widgetsSite,
    site = site,
    baseImages = baseImages,
    base = base,
    api = api,
    ips = ips,
    proxies = proxies.map { it.toDomain() }
)

fun ApiConfigProxyResponse.toDomain(): ApiProxy = ApiProxy(
    tag = tag,
    name = name,
    desc = desc,
    ip = ip,
    port = port,
    user = user,
    password = password
)