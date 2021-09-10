package ru.radiationx.anilibria.ui.fragments.youtube

import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.model.loading.DataLoadingState

data class YoutubeScreenState(
    val data: DataLoadingState<List<YoutubeItemState>> = DataLoadingState()
)