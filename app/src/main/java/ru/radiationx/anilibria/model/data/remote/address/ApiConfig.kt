package ru.radiationx.anilibria.model.data.remote.address

import ru.radiationx.anilibria.model.data.remote.Api

class ApiConfig {
    private val addresses = mutableListOf<ApiAddress>()

    @Synchronized
    fun setAddresses(items: List<ApiAddress>) {
        addresses.clear()
        addresses.addAll(items)
    }

    val active: ApiAddress?
        get() = addresses.firstOrNull()

    val tag: String?
        get() = active?.tag ?: this.javaClass.simpleName

    val name: String?
        get() = active?.name ?: "Стандартный адрес"

    val desc: String?
        get() = active?.desc

    val widgetsSiteUrl: String
        get() = active?.widgetsSite ?: Api.WIDGETS_SITE_URL

    val siteUrl: String
        get() = active?.site ?: Api.SITE_URL

    val baseImagesUrl: String
        get() = active?.baseImages ?: Api.BASE_URL_IMAGES

    val baseUrl: String
        get() = active?.base ?: Api.BASE_URL

    val apiUrl: String
        get() = active?.api ?: Api.API_URL

    val ips: List<String>
        get() = active?.ips?.let {
            if (it.isEmpty()) {
                Api.DEFAULT_IP_ADDRESSES
            } else {
                it
            }
        } ?: Api.DEFAULT_IP_ADDRESSES

    val proxies: List<ApiProxy>
        get() = active?.proxies?.let {
            if (it.isEmpty()) {
                Api.DEFAULT_PROXIES
            } else {
                it
            }
        } ?: Api.DEFAULT_PROXIES
}