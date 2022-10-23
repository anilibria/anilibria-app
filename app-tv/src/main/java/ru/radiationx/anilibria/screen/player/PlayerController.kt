package ru.radiationx.anilibria.screen.player

import kotlinx.coroutines.flow.MutableSharedFlow
import toothpick.InjectConstructor

@InjectConstructor
class PlayerController {

    val selectEpisodeRelay = MutableSharedFlow<Int>()
}