package ru.radiationx.anilibria.screen.player

import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.common.EpisodeId
import ru.radiationx.shared.ktx.EventFlow
import javax.inject.Inject

class PlayerController @Inject constructor() {

    val data = MutableStateFlow<List<Release>?>(null)

    val selectEpisodeRelay = EventFlow<EpisodeId>()

    fun reset() {
        data.value = null
    }
}