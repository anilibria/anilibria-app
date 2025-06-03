package ru.radiationx.data.app.config.models

import ru.radiationx.data.common.Url

data class AppConfigAddress(
    val id: AppConfigAddressId,
    val name: String?,
    val description: String?,
    val widget: Url.Base,
    val site: Url.Base,
    val image: Url.Base,
    val api: Url.Base,
    val status: Url.Absolute,
)