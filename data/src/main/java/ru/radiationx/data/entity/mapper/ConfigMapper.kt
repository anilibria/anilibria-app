package ru.radiationx.data.entity.mapper

import ru.radiationx.data.datasource.remote.address.ApiAddress
import ru.radiationx.data.datasource.remote.address.ApiConfigData
import ru.radiationx.data.datasource.remote.address.ApiProxy
import ru.radiationx.data.entity.response.config.ApiConfigAddressResponse
import ru.radiationx.data.entity.response.config.ApiConfigProxyResponse
import ru.radiationx.data.entity.response.config.ApiConfigResponse

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