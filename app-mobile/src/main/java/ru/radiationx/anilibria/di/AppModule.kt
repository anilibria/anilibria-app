package ru.radiationx.anilibria.di

import android.content.Context
import ru.radiationx.anilibria.AppBuildConfig
import ru.radiationx.anilibria.MobileCheckerSources
import ru.radiationx.anilibria.navigation.CiceroneHolder
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.anilibria.ui.common.ErrorHandler
import ru.radiationx.anilibria.ui.common.LinkRouter
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.radiationx.shared_app.common.OkHttpImageDownloader
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.datasource.remote.common.CheckerReserveSources
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import toothpick.config.Module

class AppModule(context: Context) : Module() {


    init {
        bind(Context::class.java).toInstance(context)

        bind(SharedBuildConfig::class.java).to(AppBuildConfig::class.java).singleton()
        bind(CheckerReserveSources::class.java).to(MobileCheckerSources::class.java).singleton()

        bind(SystemMessenger::class.java).singleton()

        val ciceroneHolder = CiceroneHolder()
        bind(CiceroneHolder::class.java).toInstance(ciceroneHolder)

        val cicerone = ciceroneHolder.getCicerone("root")
        bind(Router::class.java).toInstance(cicerone.router)
        bind(NavigatorHolder::class.java).toInstance(cicerone.navigatorHolder)


        bind(DimensionsProvider::class.java).singleton()

        bind(ILinkHandler::class.java).to(LinkRouter::class.java).singleton()
        bind(IErrorHandler::class.java).to(ErrorHandler::class.java).singleton()
        bind(OkHttpImageDownloader::class.java).singleton()

    }

}