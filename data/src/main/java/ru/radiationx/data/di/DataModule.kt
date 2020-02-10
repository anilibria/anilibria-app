package ru.radiationx.data.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import ru.radiationx.data.ApiClient
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.MainClient
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.holders.*
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.address.ApiConfigChanger
import ru.radiationx.data.datasource.remote.api.*
import ru.radiationx.data.datasource.remote.parsers.*
import ru.radiationx.data.datasource.storage.*
import ru.radiationx.data.di.providers.*
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.*
import ru.radiationx.data.system.ApiUtils
import ru.radiationx.data.system.AppCookieJar
import ru.radiationx.data.system.AppSchedulers
import toothpick.config.Module

class DataModule(context: Context) : Module() {


    init {
        val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val dataStoragePreferences = context.getSharedPreferences("${context.packageName}_datastorage", Context.MODE_PRIVATE)

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
        bind(MenuHolder::class.java).to(MenuStorage::class.java).singletonInScope()

        bind(CookieHolder::class.java).to(CookiesStorage::class.java).singletonInScope()
        bind(UserHolder::class.java).to(UserStorage::class.java).singletonInScope()
        bind(AuthHolder::class.java).to(AuthStorage::class.java).singletonInScope()

        bind(ApiConfigChanger::class.java).singletonInScope()

        bind(AppCookieJar::class.java).singletonInScope()
        bind(ApiConfig::class.java).singletonInScope()
        bind(ApiConfigStorage::class.java).singletonInScope()


        bind(MainOkHttpProvider::class.java).singletonInScope()
        bind(ApiOkHttpProvider::class.java).singletonInScope()

        bind(MainClientWrapper::class.java).singletonInScope()
        bind(ApiClientWrapper::class.java).singletonInScope()

        bind(IClient::class.java).withName(MainClient::class.java).to(MainNetworkClient::class.java).singletonInScope()
        bind(IClient::class.java).withName(ApiClient::class.java).to(ApiNetworkClient::class.java).singletonInScope()

        bind(IApiUtils::class.java).to(ApiUtils::class.java).singletonInScope()

        bind(AuthParser::class.java).singletonInScope()
        bind(CheckerParser::class.java).singletonInScope()
        bind(ConfigurationParser::class.java).singletonInScope()
        bind(PagesParser::class.java).singletonInScope()
        bind(ProfileParser::class.java).singletonInScope()
        bind(ReleaseParser::class.java).singletonInScope()
        bind(SearchParser::class.java).singletonInScope()
        bind(VitalParser::class.java).singletonInScope()
        bind(YoutubeParser::class.java).singletonInScope()
        bind(ScheduleParser::class.java).singletonInScope()
        bind(FeedParser::class.java).singletonInScope()
        bind(MenuParser::class.java).singletonInScope()

        bind(AuthApi::class.java).singletonInScope()
        bind(CheckerApi::class.java).singletonInScope()
        bind(ConfigurationApi::class.java).singletonInScope()
        bind(FavoriteApi::class.java).singletonInScope()
        bind(ReleaseApi::class.java).singletonInScope()
        bind(SearchApi::class.java).singletonInScope()
        bind(PageApi::class.java).singletonInScope()
        bind(VitalApi::class.java).singletonInScope()
        bind(YoutubeApi::class.java).singletonInScope()
        bind(ScheduleApi::class.java).singletonInScope()
        bind(FeedApi::class.java).singletonInScope()
        bind(MenuApi::class.java).singletonInScope()

        bind(AuthRepository::class.java).singletonInScope()
        bind(ReleaseRepository::class.java).singletonInScope()
        bind(ConfigurationRepository::class.java).singletonInScope()
        bind(SearchRepository::class.java).singletonInScope()
        bind(PageRepository::class.java).singletonInScope()
        bind(VitalRepository::class.java).singletonInScope()
        bind(CheckerRepository::class.java).singletonInScope()
        bind(HistoryRepository::class.java).singletonInScope()
        bind(FavoriteRepository::class.java).singletonInScope()
        bind(YoutubeRepository::class.java).singletonInScope()
        bind(ScheduleRepository::class.java).singletonInScope()
        bind(FeedRepository::class.java).singletonInScope()
        bind(MenuRepository::class.java).singletonInScope()

        bind(ReleaseInteractor::class.java).singletonInScope()
    }

}