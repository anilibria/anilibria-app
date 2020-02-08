package ru.radiationx.data.entity.app.other

data class LinkMenuItem(
        val title: String,
        val absoluteLink: String? = null,
        val sitePagePath: String? = null,
        val icon: String? = IC_SITE
) {
    companion object {
        const val IC_VK = "vk"
        const val IC_YOUTUBE = "yotube"
        const val IC_PATREON = "patreon"
        const val IC_TELEGRAM = "telegram"
        const val IC_DISCORD = "discord"
        const val IC_ANILIBRIA = "anilibria"
        const val IC_INFO = "info"
        const val IC_RULES = "rules"
        const val IC_PERSON = "person"
        const val IC_SITE = "site"
    }
}