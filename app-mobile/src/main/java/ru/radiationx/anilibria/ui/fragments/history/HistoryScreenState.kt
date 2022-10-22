package ru.radiationx.anilibria.ui.fragments.history

import ru.radiationx.anilibria.model.ReleaseItemState

data class HistoryScreenState(
    val searchItems: List<ReleaseItemState> = emptyList(),
    val items: List<ReleaseItemState> = emptyList()
)