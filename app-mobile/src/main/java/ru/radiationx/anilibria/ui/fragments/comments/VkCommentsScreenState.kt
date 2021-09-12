package ru.radiationx.anilibria.ui.fragments.comments

import ru.radiationx.anilibria.model.loading.DataLoadingState
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState

data class VkCommentsScreenState(
    val pageState: WebPageViewState? = null,
    val jsErrorVisible: Boolean = false,
    val data: DataLoadingState<VkCommentsState> = DataLoadingState()
)