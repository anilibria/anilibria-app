package ru.radiationx.data.app.config

import kotlinx.coroutines.flow.Flow
import ru.radiationx.data.app.config.models.AppConfigAddressId
import ru.radiationx.data.common.Url

interface AppConfig {

    val configState: Flow<Boolean>

    val isConfigured: Boolean

    val id: AppConfigAddressId

    val description: String?

    val widget: Url.Base

    val site: Url.Base

    val image: Url.Base

    val api: Url.Base

}