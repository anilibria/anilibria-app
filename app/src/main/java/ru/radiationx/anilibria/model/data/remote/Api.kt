package ru.radiationx.anilibria.model.data.remote

import ru.radiationx.anilibria.model.data.remote.address.ApiAddress
import ru.radiationx.anilibria.model.data.remote.address.ApiProxy

/* Created by radiationx on 31.10.17. */

object Api {
    const val WIDGETS_SITE_URL = "https://www.anilibria.tv"
    const val SITE_URL = "https://www.anilibria.tv"
    const val BASE_URL_IMAGES = "http://www.anilibria.tv/"
    const val BASE_URL = "https://www.anilibria.tv"
    const val API_URL = "https://www.anilibria.tv/public/api/index.php"
    val DEFAULT_IP_ADDRESSES = listOf("37.1.217.18")
    val DEFAULT_PROXIES = listOf<ApiProxy>(
            ApiProxy(
                    "default",
                    "Стандартный прокси",
                    null,
                    "5.187.0.24",
                    3128,
                    null,
                    null
            )
    )

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
