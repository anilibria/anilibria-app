package ru.radiationx.anilibria.model

import ru.radiationx.data.entity.common.Url
import ru.radiationx.data.entity.domain.types.YoutubeId

data class YoutubeItemState(
    val id: YoutubeId,
    val title: String,
    val image: Url.Relative?,
    val views: String,
    val comments: String
)