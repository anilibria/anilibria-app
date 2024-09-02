package ru.radiationx.anilibria.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import ru.radiationx.anilibria.R
import ru.radiationx.data.entity.domain.other.DataColor
import ru.radiationx.data.entity.domain.other.DataIcons

@DrawableRes
fun String.asDataIconRes(): Int? = when (this) {
    DataIcons.VK -> R.drawable.ic_logo_vk
    DataIcons.GOOGLE -> R.drawable.ic_logo_google
    DataIcons.YOUTUBE,
    DataIcons.YOUTUBE1,
    -> R.drawable.ic_logo_youtube

    DataIcons.PATREON -> R.drawable.ic_logo_patreon
    DataIcons.TELEGRAM -> R.drawable.ic_logo_telegram
    DataIcons.DISCORD -> R.drawable.ic_logo_discord
    DataIcons.YOOMONEY -> R.drawable.ic_logo_yoomoney
    DataIcons.DONATIONALERTS -> R.drawable.ic_logo_donationalerts
    DataIcons.BOOSTY -> R.drawable.ic_logo_boosty
    DataIcons.RUSTORE -> R.drawable.ic_logo_rustore
    DataIcons.ANILIBRIA -> R.drawable.ic_anilibria
    DataIcons.INFO -> R.drawable.ic_information
    DataIcons.RULES -> R.drawable.ic_book_open_variant
    DataIcons.PERSON -> R.drawable.ic_person
    DataIcons.SITE -> R.drawable.ic_link
    DataIcons.INFRA -> R.drawable.ic_server_plus
    else -> null
}

@ColorRes
fun String.asDataColorRes(): Int? = when (this) {
    DataColor.VK -> R.color.brand_vk
    DataColor.YOUTUBE,
    DataColor.YOUTUBE1,
    -> R.color.brand_youtube

    DataColor.PATREON -> R.color.brand_patreon
    DataColor.TELEGRAM -> R.color.brand_telegram
    DataColor.DISCORD -> R.color.brand_discord
    DataColor.YOOMONEY -> R.color.brand_yoomoney
    DataColor.DONATIONALERTS -> R.color.brand_donationalerts
    DataColor.BOOSTY -> R.color.brand_boosty
    DataColor.RUSTORE -> R.color.brand_rustore
    DataColor.ANILIBRIA -> R.color.alib_red
    else -> null
}