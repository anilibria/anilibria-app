package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.navigation.CiceroneHolder
import ru.radiationx.quill.Quill
import ru.radiationx.quill.QuillModule
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router

class RouterModule(ciceroneTag: String = ROOT) : QuillModule() {

    companion object {
        private const val ROOT = "root"
    }

    init {
        val cicerone = Quill.getRootScope().get(CiceroneHolder::class.java).getCicerone(ciceroneTag)
        instance<Router>(cicerone.router)
        instance<NavigatorHolder>(cicerone.navigatorHolder)
    }
}