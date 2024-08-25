package ru.radiationx.anilibria.model

import ru.radiationx.data.apinext.models.SocialType

data class SocialAuthItemState(
    val type: SocialType,
    val title: String,
    val iconRes: Int?,
    val colorRes: Int?
)