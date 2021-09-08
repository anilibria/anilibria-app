package ru.radiationx.anilibria.ui.fragments.search

import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.YoutubeItemState

data class SearchScreenState(
    val refreshing: Boolean = false,
    val hasMorePages: Boolean = false,
    val items: List<ReleaseItemState> = emptyList(),
    val remindText: String? = null
)