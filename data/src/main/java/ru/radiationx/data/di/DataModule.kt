@file:Suppress("DEPRECATION")

package ru.radiationx.data.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import ru.radiationx.data.ApiClient
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.MainClient
import ru.radiationx.data.SimpleClient
import ru.radiationx.data.ads.AdsConfigApi
import ru.radiationx.data.ads.AdsConfigRepository
import ru.radiationx.data.ads.AdsConfigStorage
import ru.radiationx.data.analytics.features.ActivityLaunchAnalytics
import ru.radiationx.data.analytics.features.AuthDeviceAnalytics
import ru.radiationx.data.analytics.features.AuthMainAnalytics
import ru.radiationx.data.analytics.features.AuthSocialAnalytics
import ru.radiationx.data.analytics.features.AuthVkAnalytics
import ru.radiationx.data.analytics.features.CatalogAnalytics
import ru.radiationx.data.analytics.features.CatalogFilterAnalytics
import ru.radiationx.data.analytics.features.CommentsAnalytics
import ru.radiationx.data.analytics.features.ConfiguringAnalytics
import ru.radiationx.data.analytics.features.DonationCardAnalytics
import ru.radiationx.data.analytics.features.DonationDetailAnalytics
import ru.radiationx.data.analytics.features.DonationDialogAnalytics
import ru.radiationx.data.analytics.features.DonationYooMoneyAnalytics
import ru.radiationx.data.analytics.features.FastSearchAnalytics
import ru.radiationx.data.analytics.features.FavoritesAnalytics
import ru.radiationx.data.analytics.features.FeedAnalytics
import ru.radiationx.data.analytics.features.HistoryAnalytics
import ru.radiationx.data.analytics.features.OtherAnalytics
import ru.radiationx.data.analytics.features.PlayerAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.analytics.features.ScheduleAnalytics
import ru.radiationx.data.analytics.features.SettingsAnalytics
import ru.radiationx.data.analytics.features.TeamsAnalytics
import ru.radiationx.data.analytics.features.UpdaterAnalytics
import ru.radiationx.data.analytics.features.WebPlayerAnalytics
import ru.radiationx.data.analytics.features.YoutubeAnalytics
import ru.radiationx.data.analytics.features.YoutubeVideosAnalytics
import ru.radiationx.data.analytics.profile.AnalyticsProfileDataSource
import ru.radiationx.data.datasource.holders.AuthHolder
import ru.radiationx.data.datasource.holders.CookieHolder
import ru.radiationx.data.datasource.holders.DonationHolder
import ru.radiationx.data.datasource.holders.DownloadsHolder
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.datasource.holders.GenresHolder
import ru.radiationx.data.datasource.holders.HistoryHolder
import ru.radiationx.data.datasource.holders.MenuHolder
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.datasource.holders.SocialAuthHolder
import ru.radiationx.data.datasource.holders.TeamsHolder
import ru.radiationx.data.datasource.holders.UserHolder
import ru.radiationx.data.datasource.holders.YearsHolder
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.address.ApiConfigChanger
import ru.radiationx.data.datasource.remote.api.AuthApi
import ru.radiationx.data.datasource.remote.api.CheckerApi
import ru.radiationx.data.datasource.remote.api.ConfigurationApi
import ru.radiationx.data.datasource.remote.api.DonationApi
import ru.radiationx.data.datasource.remote.api.FavoriteApi
import ru.radiationx.data.datasource.remote.api.FeedApi
import ru.radiationx.data.datasource.remote.api.MenuApi
import ru.radiationx.data.datasource.remote.api.PageApi
import ru.radiationx.data.datasource.remote.api.ReleaseApi
import ru.radiationx.data.datasource.remote.api.ScheduleApi
import ru.radiationx.data.datasource.remote.api.SearchApi
import ru.radiationx.data.datasource.remote.api.TeamsApi
import ru.radiationx.data.datasource.remote.api.YoutubeApi
import ru.radiationx.data.datasource.remote.interceptors.UnauthorizedInterceptor
import ru.radiationx.data.datasource.remote.parsers.AuthParser
import ru.radiationx.data.datasource.remote.parsers.PagesParser
import ru.radiationx.data.datasource.storage.ApiConfigStorage
import ru.radiationx.data.datasource.storage.AuthStorage
import ru.radiationx.data.datasource.storage.CookiesStorage
import ru.radiationx.data.datasource.storage.DonationStorage
import ru.radiationx.data.datasource.storage.DownloadsStorage
import ru.radiationx.data.datasource.storage.EpisodesCheckerStorage
import ru.radiationx.data.datasource.storage.GenresStorage
import ru.radiationx.data.datasource.storage.HistoryStorage
import ru.radiationx.data.datasource.storage.MenuStorage
import ru.radiationx.data.datasource.storage.PreferencesStorage
import ru.radiationx.data.datasource.storage.ReleaseUpdateStorage
import ru.radiationx.data.datasource.storage.SocialAuthStorage
import ru.radiationx.data.datasource.storage.TeamsStorage
import ru.radiationx.data.datasource.storage.UserStorage
import ru.radiationx.data.datasource.storage.YearsStorage
import ru.radiationx.data.di.providers.ApiClientWrapper
import ru.radiationx.data.di.providers.ApiNetworkClient
import ru.radiationx.data.di.providers.ApiOkHttpProvider
import ru.radiationx.data.di.providers.MainClientWrapper
import ru.radiationx.data.di.providers.MainNetworkClient
import ru.radiationx.data.di.providers.MainOkHttpProvider
import ru.radiationx.data.di.providers.PlayerOkHttpProvider
import ru.radiationx.data.di.providers.SimpleClientWrapper
import ru.radiationx.data.di.providers.SimpleNetworkClient
import ru.radiationx.data.di.providers.SimpleOkHttpProvider
import ru.radiationx.data.downloader.RemoteFileHolder
import ru.radiationx.data.downloader.RemoteFileRepository
import ru.radiationx.data.downloader.RemoteFileStorage
import ru.radiationx.data.interactors.HistoryRuntimeCache
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import ru.radiationx.data.migration.MigrationDataSource
import ru.radiationx.data.migration.MigrationDataSourceImpl
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.data.repository.ConfigurationRepository
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.data.repository.FavoriteRepository
import ru.radiationx.data.repository.FeedRepository
import ru.radiationx.data.repository.HistoryRepository
import ru.radiationx.data.repository.MenuRepository
import ru.radiationx.data.repository.PageRepository
import ru.radiationx.data.repository.ReleaseRepository
import ru.radiationx.data.repository.ScheduleRepository
import ru.radiationx.data.repository.SearchRepository
import ru.radiationx.data.repository.TeamsRepository
import ru.radiationx.data.repository.YoutubeRepository
import ru.radiationx.data.system.ApiUtils
import ru.radiationx.data.system.AppCookieJar
import ru.radiationx.quill.QuillModule
import toothpick.InjectConstructor
import javax.inject.Provider

class DataModule : QuillModule() {

    init {
        instance<Moshi> {
            Moshi.Builder().build()
        }


        singleProvider<SharedPreferences, PreferencesProvider>()
        singleProvider<SharedPreferences, DataPreferencesProvider>(DataPreferences::class)

        singleImpl<MigrationDataSource, MigrationDataSourceImpl>()

        single<PreferencesStorage>()

        singleImpl<PreferencesHolder, PreferencesStorage>()
        singleImpl<EpisodesCheckerHolder, EpisodesCheckerStorage>()
        singleImpl<HistoryHolder, HistoryStorage>()
        singleImpl<ReleaseUpdateHolder, ReleaseUpdateStorage>()
        singleImpl<GenresHolder, GenresStorage>()
        singleImpl<YearsHolder, YearsStorage>()
        singleImpl<SocialAuthHolder, SocialAuthStorage>()
        singleImpl<MenuHolder, MenuStorage>()
        singleImpl<DownloadsHolder, DownloadsStorage>()
        singleImpl<DonationHolder, DonationStorage>()
        singleImpl<TeamsHolder, TeamsStorage>()
        singleImpl<RemoteFileHolder, RemoteFileStorage>()

        singleImpl<CookieHolder, CookiesStorage>()
        singleImpl<UserHolder, UserStorage>()
        singleImpl<AuthHolder, AuthStorage>()

        single<ApiConfigChanger>()

        single<AppCookieJar>()
        single<UnauthorizedInterceptor>()
        single<ApiConfig>()
        single<ApiConfigStorage>()


        single<PlayerOkHttpProvider>()
        single<SimpleOkHttpProvider>()
        single<MainOkHttpProvider>()
        single<ApiOkHttpProvider>()

        single<SimpleClientWrapper>()
        single<MainClientWrapper>()
        single<ApiClientWrapper>()

        singleImpl<IClient, SimpleNetworkClient>(SimpleClient::class)
        singleImpl<IClient, MainNetworkClient>(MainClient::class)
        singleImpl<IClient, ApiNetworkClient>(ApiClient::class)

        singleImpl<IApiUtils, ApiUtils>()

        single<AuthParser>()
        single<PagesParser>()
        single<PagesParser>()

        single<AuthApi>()
        single<CheckerApi>()
        single<ConfigurationApi>()
        single<FavoriteApi>()
        single<ReleaseApi>()
        single<SearchApi>()
        single<PageApi>()
        single<YoutubeApi>()
        single<ScheduleApi>()
        single<FeedApi>()
        single<MenuApi>()
        single<DonationApi>()
        single<TeamsApi>()

        single<AuthRepository>()
        single<ReleaseRepository>()
        single<ConfigurationRepository>()
        single<SearchRepository>()
        single<PageRepository>()
        single<CheckerRepository>()
        single<HistoryRepository>()
        single<FavoriteRepository>()
        single<YoutubeRepository>()
        single<ScheduleRepository>()
        single<FeedRepository>()
        single<MenuRepository>()
        single<DonationRepository>()
        single<TeamsRepository>()
        single<RemoteFileRepository>()

        single<ReleaseUpdateMiddleware>()

        single<ReleaseInteractor>()

        single<HistoryRuntimeCache>()


        /* Analytics */
        single<ActivityLaunchAnalytics>()
        single<AnalyticsProfileDataSource>()
        single<AuthDeviceAnalytics>()
        single<AuthMainAnalytics>()
        single<AuthSocialAnalytics>()
        single<AuthVkAnalytics>()
        single<CatalogAnalytics>()
        single<CatalogFilterAnalytics>()
        single<CommentsAnalytics>()
        single<ConfiguringAnalytics>()
        single<FastSearchAnalytics>()
        single<FavoritesAnalytics>()
        single<FeedAnalytics>()
        single<HistoryAnalytics>()
        single<OtherAnalytics>()
        single<PlayerAnalytics>()
        single<ReleaseAnalytics>()
        single<ScheduleAnalytics>()
        single<SettingsAnalytics>()
        single<UpdaterAnalytics>()
        single<WebPlayerAnalytics>()
        single<YoutubeAnalytics>()
        single<YoutubeVideosAnalytics>()
        single<DonationCardAnalytics>()
        single<DonationDetailAnalytics>()
        single<DonationDialogAnalytics>()
        single<DonationYooMoneyAnalytics>()
        single<TeamsAnalytics>()

        /* Ads */
        single<AdsConfigApi>()
        single<AdsConfigStorage>()
        single<AdsConfigRepository>()
    }


    @InjectConstructor
    class PreferencesProvider(
        private val context: Context,
    ) : Provider<SharedPreferences> {
        @Suppress("DEPRECATION")
        override fun get(): SharedPreferences {
            // for strict-mode pass
            return runBlocking {
                withContext(Dispatchers.IO) {
                    PreferenceManager.getDefaultSharedPreferences(context)
                }
            }
        }
    }

    @InjectConstructor
    class DataPreferencesProvider(
        private val context: Context,
    ) : Provider<SharedPreferences> {
        override fun get(): SharedPreferences {
            // for strict-mode pass
            return runBlocking {
                withContext(Dispatchers.IO) {
                    context.getSharedPreferences(
                        "${context.packageName}_datastorage",
                        Context.MODE_PRIVATE
                    )
                }
            }
        }
    }

}