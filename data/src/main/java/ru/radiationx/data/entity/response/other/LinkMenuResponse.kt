package ru.radiationx.data.entity.response.other

data class LinkMenuResponse(
    val title: String,
    val absoluteLink: String? = null,
    val sitePagePath: String? = null,
    val icon: String? = null
)