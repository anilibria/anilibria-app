package ru.radiationx.anilibria.screen.player

import kotlinx.coroutines.flow.MutableSharedFlow
import ru.radiationx.data.entity.domain.types.EpisodeId
import toothpick.InjectConstructor

@InjectConstructor
class PlayerController {

    val selectEpisodeRelay = MutableSharedFlow<EpisodeId>()
}