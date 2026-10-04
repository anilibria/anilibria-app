package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.screen.player.PlayerController
import ru.radiationx.anilibria.screen.player.quality.PlayerQualityPreference
import ru.radiationx.quill.QuillModule

class PlayerModule : QuillModule() {

    init {
        single<PlayerController>()
        single<PlayerQualityPreference>()
    }
}
