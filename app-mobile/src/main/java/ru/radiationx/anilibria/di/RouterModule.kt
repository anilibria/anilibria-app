package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.navigation.CiceroneHolder
import ru.radiationx.quill.Quill
import ru.radiationx.quill.QuillModule

class RouterModule(ciceroneTag: String) : QuillModule() {

    init {
        val cicerone by lazy {
            Quill.getRootScope().get(CiceroneHolder::class).getCicerone(ciceroneTag)
        }
        instance {
            cicerone.router
        }
        instance {
            cicerone.navigatorHolder
        }
    }
}