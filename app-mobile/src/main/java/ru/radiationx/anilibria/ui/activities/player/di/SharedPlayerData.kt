package ru.radiationx.anilibria.ui.activities.player.di

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.anilibria.ui.activities.player.models.LoadingState
import ru.radiationx.anilibria.ui.activities.player.models.PlayerData
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.shared.ktx.EventFlow

class SharedPlayerData(episodeId: EpisodeId) {

    val episodeId = MutableStateFlow(episodeId)

    val dataState = MutableStateFlow(LoadingState<PlayerData>())

    val onEpisodeSelected = EventFlow<EpisodeId>()
}