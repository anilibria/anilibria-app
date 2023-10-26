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
import ru.radiationx.data.analytics.features.*
import ru.radiationx.data.analytics.profile.AnalyticsProfileDataSource
import ru.radiationx.data.datasource.holders.*
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.address.ApiConfigChanger
import ru.radiationx.data.datasource.remote.api.*
import ru.radiationx.data.datasource.remote.interceptors.UnauthorizedInterceptor
import ru.radiationx.data.datasource.remote.parsers.AuthParser
import ru.radiationx.data.datasource.remote.parsers.PagesParser
import ru.radiationx.data.datasource.storage.*
import ru.radiationx.data.di.providers.*
import ru.radiationx.data.downloader.RemoteFileRepository
import ru.radiationx.data.downloader.RemoteFileHolder
import ru.radiationx.data.downloader.RemoteFileStorage
import ru.radiationx.data.interactors.HistoryRuntimeCache
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import ru.radiationx.data.migration.MigrationDataSource
import ru.radiationx.data.migration.MigrationDataSourceImpl
import ru.radiationx.data.repository.*
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