package ru.radiationx.data.datasource.remote

import ru.radiationx.data.datasource.remote.address.ApiAddress
import ru.radiationx.data.datasource.remote.address.ApiProxy

/* Created by radiationx on 31.10.17. */

object Api {
    private const val WIDGETS_SITE_URL = "https://www.anilibria.tv"
    private const val SITE_URL = "https://www.anilibria.tv"
    private const val BASE_URL_IMAGES = "https://static.anilibria.tv/"
    private const val BASE_URL = "https://www.anilibria.tv"
    private const val API_URL = "https://www.anilibria.tv/public/api/index.php"
    private val DEFAULT_IP_ADDRESSES = listOf<String>()
    private val DEFAULT_PROXIES = listOf<ApiProxy>()

    val DEFAULT_ADDRESS = ApiAddress(
        "default",
        "Стандартный домен",
        "",
        WIDGETS_SITE_URL,
        SITE_URL,
        BASE_URL_IMAGES,
        BASE_URL,
        API_URL,
        DEFAULT_IP_ADDRESSES,
        DEFAULT_PROXIES
    )
}
