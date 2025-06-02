package ru.radiationx.data.app.menu.models

import ru.radiationx.data.common.Url

data class LinkMenuItem(
    val title: String,
    val link: Url.Absolute? = null,
    val pagePath: Url.Path? = null,
    val icon: String? = null
)