package ru.radiationx.anilibria.ui.fragments.comments

import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoaderState

data class VkCommentsScreenState(
    val pageState: WebPageViewState = WebPageViewState.Loading,
    val jsErrorVisible: Boolean = false,
    val vkBlockedVisible: Boolean = false,
    val data: SingleLoaderState<VkCommentsState> = SingleLoaderState.empty()
)