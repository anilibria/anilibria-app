package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.navigation.CiceroneHolder
import ru.radiationx.quill.Quill
import ru.radiationx.quill.QuillModule
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router

class RouterModule(ciceroneTag: String) : QuillModule() {

    init {
        val cicerone by lazy {
            Quill.getRootScope().get(CiceroneHolder::class).getCicerone(ciceroneTag)
        }
        instance<Router> {
            cicerone.router
        }
        instance<NavigatorHolder> {
            cicerone.navigatorHolder
        }
    }
}