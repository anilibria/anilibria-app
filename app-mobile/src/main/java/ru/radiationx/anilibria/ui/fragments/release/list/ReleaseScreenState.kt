package ru.radiationx.anilibria.ui.fragments.release.list

import ru.radiationx.anilibria.model.ReleaseItemState

data class ReleaseScreenState(
    val refreshing: Boolean = false,
    val hasMorePages: Boolean = false,
    val items: List<ReleaseItemState> = emptyList()
)