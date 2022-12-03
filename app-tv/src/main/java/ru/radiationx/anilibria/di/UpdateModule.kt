package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.screen.update.UpdateController
import ru.radiationx.quill.QuillModule

class UpdateModule : QuillModule() {

    init {
        single<UpdateController>()
    }
}