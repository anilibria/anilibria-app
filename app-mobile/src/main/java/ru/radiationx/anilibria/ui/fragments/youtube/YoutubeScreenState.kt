package ru.radiationx.anilibria.ui.fragments.youtube

import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderState

data class YoutubeScreenState(
    val data: PageLoaderState<List<YoutubeItemState>> = PageLoaderState()
)