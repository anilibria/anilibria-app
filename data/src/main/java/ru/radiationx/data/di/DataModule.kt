package ru.radiationx.data.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import ru.radiationx.data.ApiClient
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.MainClient
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.analytics.features.*
import ru.radiationx.data.analytics.profile.AnalyticsProfileDataSource
import ru.radiationx.data.datasource.holders.*
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.address.ApiConfigChanger
import ru.radiationx.data.datasource.remote.api.*
import ru.radiationx.data.datasource.remote.parsers.*
import ru.radiationx.data.datasource.storage.*
import ru.radiationx.data.di.providers.*
import ru.radiationx.data.interactors.ConfiguringInteractor
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.migration.MigrationDataSource
import ru.radiationx.data.migration.MigrationDataSourceImpl
import ru.radiationx.data.repository.*
import ru.radiationx.data.system.ApiUtils
import ru.radiationx.data.system.AppCookieJar
import ru.radiationx.data.system.AppSchedulers
import toothpick.config.Module

class DataModule(context: Context) : Module() {


    init {
        val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val dataStoragePreferences =
            context.getSharedPreferences("${context.packageName}_datastorage", Context.MODE_PRIVATE)

        bind(SchedulersProvider::class.java).to(AppSchedulers::class.java).singleton()

        bind(Gson::class.java).toInstance(Gson())

        bind(SharedPreferences::class.java).toInstance(defaultPreferences)
        bind(SharedPreferences::class.java).withName(DataPreferences::class.java)
            .toInstance(dataStoragePreferences)

        bind(MigrationDataSource::class.java).to(MigrationDataSourceImpl::class.java).singleton()

        bind(PreferencesStorage::class.java).singleton()

        bind(PreferencesHolder::class.java).to(PreferencesStorage::class.java).singleton()
        bind(AppThemeHolder::class.java).to(PreferencesStorage::class.java).singleton()
        bind(EpisodesCheckerHolder::class.java).to(EpisodesCheckerStorage::class.java).singleton()
        bind(HistoryHolder::class.java).to(HistoryStorage::class.java).singleton()
        bind(ReleaseUpdateHolder::class.java).to(ReleaseUpdateStorage::class.java).singleton()
        bind(GenresHolder::class.java).to(GenresStorage::class.java).singleton()
        bind(YearsHolder::class.java).to(YearsStorage::class.java).singleton()
        bind(SocialAuthHolder::class.java).to(SocialAuthStorage::class.java).singleton()
        bind(MenuHolder::class.java).to(MenuStorage::class.java).singleton()
        bind(DownloadsHolder::class.java).to(DownloadsStorage::class.java).singleton()
        bind(DonationHolder::class.java).to(DonationStorage::class.java).singleton()

        bind(CookieHolder::class.java).to(CookiesStorage::class.java).singleton()
        bind(UserHolder::class.java).to(UserStorage::class.java).singleton()
        bind(AuthHolder::class.java).to(AuthStorage::class.java).singleton()

        bind(ApiConfigChanger::class.java).singleton()

        bind(AppCookieJar::class.java).singleton()
        bind(ApiConfig::class.java).singleton()
        bind(ApiConfigStorage::class.java).singleton()


        bind(MainOkHttpProvider::class.java).singleton()
        bind(ApiOkHttpProvider::class.java).singleton()

        bind(MainClientWrapper::class.java).singleton()
        bind(ApiClientWrapper::class.java).singleton()

        bind(IClient::class.java).withName(MainClient::class.java).to(MainNetworkClient::class.java)
            .singleton()
        bind(IClient::class.java).withName(ApiClient::class.java).to(ApiNetworkClient::class.java)
            .singleton()

        bind(IApiUtils::class.java).to(ApiUtils::class.java).singleton()

        bind(AuthParser::class.java).singleton()
        bind(CheckerParser::class.java).singleton()
        bind(ConfigurationParser::class.java).singleton()
        bind(PagesParser::class.java).singleton()
        bind(ProfileParser::class.java).singleton()
        bind(ReleaseParser::class.java).singleton()
        bind(SearchParser::class.java).singleton()
        bind(YoutubeParser::class.java).singleton()
        bind(ScheduleParser::class.java).singleton()
        bind(FeedParser::class.java).singleton()
        bind(MenuParser::class.java).singleton()

        bind(AuthApi::class.java).singleton()
        bind(CheckerApi::class.java).singleton()
        bind(ConfigurationApi::class.java).singleton()
        bind(FavoriteApi::class.java).singleton()
        bind(ReleaseApi::class.java).singleton()
        bind(SearchApi::class.java).singleton()
        bind(PageApi::class.java).singleton()
        bind(YoutubeApi::class.java).singleton()
        bind(ScheduleApi::class.java).singleton()
        bind(FeedApi::class.java).singleton()
        bind(MenuApi::class.java).singleton()
        bind(DonationApi::class.java).singleton()

        bind(AuthRepository::class.java).singleton()
        bind(ReleaseRepository::class.java).singleton()
        bind(ConfigurationRepository::class.java).singleton()
        bind(SearchRepository::class.java).singleton()
        bind(PageRepository::class.java).singleton()
        bind(CheckerRepository::class.java).singleton()
        bind(HistoryRepository::class.java).singleton()
        bind(FavoriteRepository::class.java).singleton()
        bind(YoutubeRepository::class.java).singleton()
        bind(ScheduleRepository::class.java).singleton()
        bind(FeedRepository::class.java).singleton()
        bind(MenuRepository::class.java).singleton()
        bind(DonationRepository::class.java).singleton()

        bind(ReleaseInteractor::class.java).singleton()
        bind(ConfiguringInteractor::class.java).singleton()


        /* Analytics */
        bind(AnalyticsProfileDataSource::class.java).singleton()
        bind(AppAnalytics::class.java).singleton()
        bind(AuthDeviceAnalytics::class.java).singleton()
        bind(AuthMainAnalytics::class.java).singleton()
        bind(AuthSocialAnalytics::class.java).singleton()
        bind(AuthVkAnalytics::class.java).singleton()
        bind(CatalogAnalytics::class.java).singleton()
        bind(CatalogFilterAnalytics::class.java).singleton()
        bind(CommentsAnalytics::class.java).singleton()
        bind(ConfiguringAnalytics::class.java).singleton()
        bind(FastSearchAnalytics::class.java).singleton()
        bind(FavoritesAnalytics::class.java).singleton()
        bind(FeedAnalytics::class.java).singleton()
        bind(HistoryAnalytics::class.java).singleton()
        bind(OtherAnalytics::class.java).singleton()
        bind(PlayerAnalytics::class.java).singleton()
        bind(ReleaseAnalytics::class.java).singleton()
        bind(ScheduleAnalytics::class.java).singleton()
        bind(SettingsAnalytics::class.java).singleton()
        bind(UpdaterAnalytics::class.java).singleton()
        bind(WebPlayerAnalytics::class.java).singleton()
        bind(YoutubeAnalytics::class.java).singleton()
        bind(YoutubeVideosAnalytics::class.java).singleton()
        bind(DonationCardAnalytics::class.java).singleton()
        bind(DonationDetailAnalytics::class.java).singleton()
        bind(DonationDialogAnalytics::class.java).singleton()
        bind(DonationYooMoneyAnalytics::class.java).singleton()
    }

}