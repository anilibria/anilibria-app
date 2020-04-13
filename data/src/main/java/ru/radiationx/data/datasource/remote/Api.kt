package ru.radiationx.data.datasource.remote

import ru.radiationx.data.datasource.remote.address.ApiAddress
import ru.radiationx.data.datasource.remote.address.ApiProxy

/* Created by radiationx on 31.10.17. */

object Api {
    const val WIDGETS_SITE_URL = "https://www.anilibria.tv"
    const val SITE_URL = "https://www.anilibria.tv"
    const val BASE_URL_IMAGES = "https://static.anilibria.tv/"
    const val BASE_URL = "https://www.anilibria.tv"
    const val API_URL = "https://www.anilibria.tv/public/api/index.php"
    val DEFAULT_IP_ADDRESSES = listOf<String>()
    val DEFAULT_PROXIES = listOf<ApiProxy>()

    val STORE_APP_IDS = arrayOf<String>()

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
