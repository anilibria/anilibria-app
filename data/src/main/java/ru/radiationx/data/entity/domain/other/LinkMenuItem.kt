package ru.radiationx.data.entity.domain.other

import ru.radiationx.data.entity.common.Url

data class LinkMenuItem(
    val title: String,
    val link: Url.Absolute? = null,
    val pagePath: Url.Relative? = null,
    val icon: String? = null
)