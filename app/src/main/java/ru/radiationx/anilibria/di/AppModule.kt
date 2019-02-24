package ru.radiationx.anilibria.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import ru.radiationx.anilibria.Client
import ru.radiationx.anilibria.di.qualifier.DataPreferences
import ru.radiationx.anilibria.model.data.holders.*
import ru.radiationx.anilibria.model.data.remote.IAntiDdosErrorHandler
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.api.*
import ru.radiationx.anilibria.model.data.remote.parsers.*
import ru.radiationx.anilibria.model.data.storage.*
import ru.radiationx.anilibria.model.interactors.AntiDdosInteractor
import ru.radiationx.anilibria.model.interactors.ReleaseInteractor
import ru.radiationx.anilibria.model.repository.*
import ru.radiationx.anilibria.model.system.ApiUtils
import ru.radiationx.anilibria.model.system.AppSchedulers
import ru.radiationx.anilibria.model.system.SchedulersProvider
import ru.radiationx.anilibria.model.system.messages.SystemMessenger
import ru.radiationx.anilibria.navigation.CiceroneHolder
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.anilibria.ui.common.AntiDdosErrorHandler
import ru.radiationx.anilibria.ui.common.ErrorHandler
import ru.radiationx.anilibria.ui.common.LinkRouter
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import toothpick.config.Module

class AppModule(context: Context) : Module() {


    init {
        bind(Context::class.java).toInstance(context)

        val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val dataStoragePreferences = context.getSharedPreferences("${context.packageName}_datastorage", Context.MODE_PRIVATE)

        val ciceroneHolder = CiceroneHolder()
        bind(CiceroneHolder::class.java).toInstance(ciceroneHolder)

        val cicerone = ciceroneHolder.getCicerone("root")
        bind(Router::class.java).toInstance(cicerone.router)
        bind(NavigatorHolder::class.java).toInstance(cicerone.navigatorHolder)

        bind(SystemMessenger::class.java).singletonInScope()

        bind(DimensionsProvider::class.java).singletonInScope()
        bind(SchedulersProvider::class.java).to(AppSchedulers::class.java).singletonInScope()



        bind(SharedPreferences::class.java).toInstance(defaultPreferences)
        bind(SharedPreferences::class.java).withName(DataPreferences::class.java).toInstance(dataStoragePreferences)

        bind(PreferencesStorage::class.java).singletonInScope()

        bind(PreferencesHolder::class.java).to(PreferencesStorage::class.java).singletonInScope()
        bind(AppThemeHolder::class.java).to(PreferencesStorage::class.java).singletonInScope()
        bind(EpisodesCheckerHolder::class.java).to(EpisodesCheckerStorage::class.java).singletonInScope()
        bind(HistoryHolder::class.java).to(HistoryStorage::class.java).singletonInScope()
        bind(ReleaseUpdateHolder::class.java).to(ReleaseUpdateStorage::class.java).singletonInScope()
        bind(GenresHolder::class.java).to(GenresStorage::class.java).singletonInScope()
        bind(YearsHolder::class.java).to(YearsStorage::class.java).singletonInScope()
        bind(SocialAuthHolder::class.java).to(SocialAuthStorage::class.java).singletonInScope()


        bind(ILinkHandler::class.java).to(LinkRouter::class.java).singletonInScope()
        bind(IErrorHandler::class.java).to(ErrorHandler::class.java).singletonInScope()
        bind(IAntiDdosErrorHandler::class.java).to(AntiDdosErrorHandler::class.java).singletonInScope()


        bind(CookieHolder::class.java).to(CookiesStorage::class.java).singletonInScope()
        bind(UserHolder::class.java).to(UserStorage::class.java).singletonInScope()
        bind(AuthHolder::class.java).to(AuthStorage::class.java).singletonInScope()


        bind(IClient::class.java).to(Client::class.java).singletonInScope()
        bind(IApiUtils::class.java).to(ApiUtils::class.java).singletonInScope()

        bind(ArticleParser::class.java).singletonInScope()
        bind(AuthParser::class.java).singletonInScope()
        bind(CheckerParser::class.java).singletonInScope()
        bind(CommentParser::class.java).singletonInScope()
        bind(FavoriteParser::class.java).singletonInScope()
        bind(PagesParser::class.java).singletonInScope()
        bind(ProfileParser::class.java).singletonInScope()
        bind(ReleaseParser::class.java).singletonInScope()
        bind(SearchParser::class.java).singletonInScope()
        bind(VitalParser::class.java).singletonInScope()
        bind(YoutubeParser::class.java).singletonInScope()

        bind(ArticleApi::class.java).singletonInScope()
        bind(AuthApi::class.java).singletonInScope()
        bind(CheckerApi::class.java).singletonInScope()
        bind(CommentApi::class.java).singletonInScope()
        bind(FavoriteApi::class.java).singletonInScope()
        bind(ReleaseApi::class.java).singletonInScope()
        bind(SearchApi::class.java).singletonInScope()
        bind(PageApi::class.java).singletonInScope()
        bind(VitalApi::class.java).singletonInScope()
        bind(YoutubeApi::class.java).singletonInScope()

        bind(AuthRepository::class.java).singletonInScope()
        bind(ArticleRepository::class.java).singletonInScope()
        bind(ReleaseRepository::class.java).singletonInScope()
        bind(SearchRepository::class.java).singletonInScope()
        bind(PageRepository::class.java).singletonInScope()
        bind(VitalRepository::class.java).singletonInScope()
        bind(CheckerRepository::class.java).singletonInScope()
        bind(HistoryRepository::class.java).singletonInScope()
        bind(FavoriteRepository::class.java).singletonInScope()
        bind(YoutubeRepository::class.java).singletonInScope()
        bind(CommentsRepository::class.java).singletonInScope()

        bind(ReleaseInteractor::class.java).singletonInScope()
        bind(AntiDdosInteractor::class.java).singletonInScope()
    }

}