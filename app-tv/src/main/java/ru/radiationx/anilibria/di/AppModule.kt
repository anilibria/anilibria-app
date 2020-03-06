package ru.radiationx.anilibria.di

import android.content.Context
import ru.radiationx.anilibria.AppBuildConfig
import ru.radiationx.anilibria.common.fragment.DialogRouter
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.shared_app.common.OkHttpImageDownloader
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import toothpick.config.Module

class AppModule(context: Context) : Module() {


    init {
        bind(Context::class.java).toInstance(context)
        bind(SharedBuildConfig::class.java).to(AppBuildConfig::class.java).singleton()
        bind(OkHttpImageDownloader::class.java).singleton()


        // Navigation
        val cicerone = Cicerone.create(DialogRouter())
        bind(Router::class.java).toInstance(cicerone.router)
        bind(DialogRouter::class.java).toInstance(cicerone.router)
        bind(NavigatorHolder::class.java).toInstance(cicerone.navigatorHolder)
    }

}