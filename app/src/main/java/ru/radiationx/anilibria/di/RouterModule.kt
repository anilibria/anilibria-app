package ru.radiationx.anilibria.di

import ru.radiationx.anilibria.di.extensions.DI
import ru.radiationx.anilibria.navigation.LocalCiceroneHolder
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import toothpick.config.Module

class RouterModule(ciceroneTag: String) : Module() {

    init {
        val cicerone = DI.get(LocalCiceroneHolder::class.java).getCicerone(ciceroneTag)
        bind(Router::class.java).toInstance(cicerone.router)
        bind(NavigatorHolder::class.java).toInstance(cicerone.navigatorHolder)
    }
}