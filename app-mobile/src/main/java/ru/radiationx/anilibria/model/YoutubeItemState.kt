package ru.radiationx.anilibria.model

import ru.radiationx.data.common.Url
import ru.radiationx.data.common.YoutubeId

data class YoutubeItemState(
    val id: YoutubeId,
    val title: String,
    val image: Url.Path?,
    val views: String,
    val comments: String
)