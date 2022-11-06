package ru.radiationx.data.entity.domain.other

data class LinkMenuItem(
    val title: String,
    val absoluteLink: String? = null,
    val sitePagePath: String? = null,
    val icon: String? = null
)