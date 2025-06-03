package ru.radiationx.data.app.config

import ru.radiationx.data.app.config.models.ApiAddressId
import ru.radiationx.data.common.Url

interface ApiConfig {

    val id: ApiAddressId

    val description: String?

    val widget: Url.Base

    val site: Url.Base

    val image: Url.Base

    val api: Url.Base

}