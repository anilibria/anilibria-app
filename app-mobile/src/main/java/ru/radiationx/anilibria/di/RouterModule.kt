package ru.radiationx.anilibria.di

import android.util.Log
import ru.radiationx.shared_app.di.DI
import ru.radiationx.anilibria.navigation.CiceroneHolder
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import toothpick.config.Module

class RouterModule(ciceroneTag: String = ROOT) : Module() {

    companion object {
        private const val ROOT = "root"
    }

    init {
        val cicerone = DI.get(CiceroneHolder::class.java).getCicerone(ciceroneTag)

        Log.e("lalala", "router inst '$ciceroneTag': ${cicerone.router}")
        bind(Router::class.java).toInstance(cicerone.router)
        bind(NavigatorHolder::class.java).toInstance(cicerone.navigatorHolder)
    }
}