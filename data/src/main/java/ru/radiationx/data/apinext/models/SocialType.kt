package ru.radiationx.data.apinext.models

import ru.radiationx.data.entity.domain.other.DataIcons

enum class SocialType(val key: String) {
    VK(DataIcons.VK),
    GOOGLE(DataIcons.GOOGLE),
    PATREON(DataIcons.PATREON),
    DISCORD(DataIcons.DISCORD)
}