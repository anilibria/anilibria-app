package ru.radiationx.anilibria.di

import android.content.Context
import ru.radiationx.anilibria.AppBuildConfig
import ru.radiationx.anilibria.TvCheckerSources
import ru.radiationx.anilibria.common.MockData
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.datasource.remote.common.CheckerReserveSources
import ru.radiationx.shared_app.common.OkHttpImageDownloader
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import toothpick.config.Module

class AppModule(context: Context) : Module() {


    init {
        bind(Context::class.java).toInstance(context)
        bind(SharedBuildConfig::class.java).to(AppBuildConfig::class.java).singleton()
        bind(CheckerReserveSources::class.java).to(TvCheckerSources::class.java).singleton()

        bind(OkHttpImageDownloader::class.java).singleton()

        bind(MockData::class.java).singleton()
    }

}