package ru.radiationx.anilibria.ui.fragments.youtube

import ru.radiationx.anilibria.model.YoutubeItemState

data class YoutubeListState(
    val refreshing: Boolean = false,
    val hasMorePages: Boolean = false,
    val items: List<YoutubeItemState> = emptyList()
)