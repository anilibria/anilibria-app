package ru.radiationx.data.app.config

import ru.radiationx.data.app.config.models.AppConfigAddressId
import ru.radiationx.data.common.Url

interface AppConfig {

    val id: AppConfigAddressId

    val description: String?

    val widget: Url.Base

    val site: Url.Base

    val image: Url.Base

    val api: Url.Base

}