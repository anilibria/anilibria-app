package ru.radiationx.anilibria.screen.player

import com.jakewharton.rxrelay2.PublishRelay
import toothpick.InjectConstructor

@InjectConstructor
class PlayerController {

    val selectEpisodeRelay = PublishRelay.create<Int>()
}