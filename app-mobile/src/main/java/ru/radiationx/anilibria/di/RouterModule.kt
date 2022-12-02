package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.navigation.CiceroneHolder
import ru.radiationx.quill.Quill
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import toothpick.config.Module

class RouterModule(ciceroneTag: String = ROOT) : Module() {

    companion object {
        private const val ROOT = "root"
    }

    init {
        val cicerone = Quill.getRootScope().get(CiceroneHolder::class.java).getCicerone(ciceroneTag)
        bind(Router::class.java).toInstance(cicerone.router)
        bind(NavigatorHolder::class.java).toInstance(cicerone.navigatorHolder)
    }
}