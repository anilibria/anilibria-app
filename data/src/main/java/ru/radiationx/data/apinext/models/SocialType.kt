package ru.radiationx.data.apinext.models

import ru.radiationx.data.entity.domain.other.DataIcons

enum class SocialType(val key: String, val enabled: Boolean) {
    VK(DataIcons.VK, true),
    // todo API2 needs implement auth with browser
    GOOGLE(DataIcons.GOOGLE, false),
    PATREON(DataIcons.PATREON, true),
    DISCORD(DataIcons.DISCORD, true)
}