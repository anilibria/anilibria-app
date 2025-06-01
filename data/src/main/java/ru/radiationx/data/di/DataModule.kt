@file:Suppress("DEPRECATION")

package ru.radiationx.data.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import anilibria.api.auth.AuthApi
import anilibria.api.catalog.CatalogApi
import anilibria.api.collections.CollectionsApi
import anilibria.api.favorites.FavoritesApi
import anilibria.api.franchises.FranchisesApi
import anilibria.api.genres.GenresApi
import anilibria.api.profile.ProfileApi
import anilibria.api.releases.ReleasesApi
import anilibria.api.schedule.ScheduleApi
import anilibria.api.shared.errors.ApiErrorParser
import anilibria.api.teams.TeamsApi
import anilibria.api.timecodes.TimeCodesApi
import anilibria.api.torrent.TorrentsApi
import anilibria.api.videos.VideosApi
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import ru.radiationx.data.ApiClient
import ru.radiationx.data.ApiRetrofit
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.DirectClient
import ru.radiationx.data.DirectRetrofit
import ru.radiationx.data.PlayerClient
import ru.radiationx.data.R
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
import ru.radiationx.data.analytics.features.SslCompatAnalytics
import ru.radiationx.data.analytics.features.TeamsAnalytics
import ru.radiationx.data.analytics.features.UpdaterAnalytics
import ru.radiationx.data.analytics.features.WebPlayerAnalytics
import ru.radiationx.data.analytics.features.YoutubeAnalytics
import ru.radiationx.data.analytics.features.YoutubeVideosAnalytics
import ru.radiationx.data.analytics.profile.AnalyticsInstallerProfileDataSource
import ru.radiationx.data.analytics.profile.AnalyticsMainProfileDataSource
import ru.radiationx.data.apinext.AuthTokenInterceptor
import ru.radiationx.data.apinext.AuthTokenStorage
import ru.radiationx.data.apinext.datasources.AuthApiDataSource
import ru.radiationx.data.apinext.datasources.CatalogApiDataSource
import ru.radiationx.data.apinext.datasources.CollectionsApiDataSource
import ru.radiationx.data.apinext.datasources.FavoritesApiDataSource
import ru.radiationx.data.apinext.datasources.FranchisesApiDataSource
import ru.radiationx.data.apinext.datasources.ProfileApiDataSource
import ru.radiationx.data.apinext.datasources.ReleasesApiDataSource
import ru.radiationx.data.apinext.datasources.ScheduleApiDataSource
import ru.radiationx.data.apinext.datasources.TeamsApiDataSource
import ru.radiationx.data.apinext.datasources.TimeCodesApiDataSource
import ru.radiationx.data.apinext.datasources.TorrentsApiDataSource
import ru.radiationx.data.apinext.datasources.VideosApiDataSource
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
import ru.radiationx.data.datasource.holders.UserHolder
import ru.radiationx.data.datasource.holders.YearsHolder
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.address.ApiConfigChanger
import ru.radiationx.data.datasource.remote.api.CheckerApiDataSource
import ru.radiationx.data.datasource.remote.api.ConfigurationApiDataSource
import ru.radiationx.data.datasource.remote.api.DirectApi
import ru.radiationx.data.datasource.remote.api.DonationApiDataSource
import ru.radiationx.data.datasource.remote.api.MenuApiDataSource
import ru.radiationx.data.datasource.remote.interceptors.AppInfoInterceptor
import ru.radiationx.data.datasource.remote.interceptors.UnauthorizedInterceptor
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
import ru.radiationx.data.datasource.storage.UserStorage
import ru.radiationx.data.datasource.storage.YearsStorage
import ru.radiationx.data.di.providers.ApiOkhttpProvider
import ru.radiationx.data.di.providers.DirectOkHttpProvider
import ru.radiationx.data.di.providers.PlayerOkHttpProvider
import ru.radiationx.data.downloader.RemoteFileHolder
import ru.radiationx.data.downloader.RemoteFileRepository
import ru.radiationx.data.downloader.RemoteFileStorage
import ru.radiationx.data.interactors.CollectionsInteractor
import ru.radiationx.data.interactors.FavoritesInteractor
import ru.radiationx.data.interactors.FilterInteractor
import ru.radiationx.data.interactors.HistoryRuntimeCache
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import ru.radiationx.data.migration.MigrationDataSource
import ru.radiationx.data.migration.MigrationDataSourceImpl
import ru.radiationx.data.player.PlayerCacheDataSourceProvider
import ru.radiationx.data.player.PlayerDataSourceProvider
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.CatalogRepository
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.data.repository.CollectionsRepository
import ru.radiationx.data.repository.ConfigurationRepository
import ru.radiationx.data.repository.DonationRepository
import ru.radiationx.data.repository.FavoriteRepository
import ru.radiationx.data.repository.FeedRepository
import ru.radiationx.data.repository.FranchisesRepository
import ru.radiationx.data.repository.HistoryRepository
import ru.radiationx.data.repository.MenuRepository
import ru.radiationx.data.repository.ReleaseRepository
import ru.radiationx.data.repository.ScheduleRepository
import ru.radiationx.data.repository.SearchRepository
import ru.radiationx.data.repository.TeamsRepository
import ru.radiationx.data.repository.VkCommentsRepository
import ru.radiationx.data.repository.YoutubeRepository
import ru.radiationx.data.sslcompat.SslCompat
import ru.radiationx.data.system.AppCookieJar
import ru.radiationx.data.system.DataErrorMapper
import ru.radiationx.data.system.UserAgentGenerator
import ru.radiationx.quill.QuillModule
import javax.inject.Inject
import javax.inject.Provider

class DataModule(context: Context) : QuillModule() {

    init {


        instance<SslCompat> {
            val rawCertResources = listOf(
                R.raw.gsr4,
                R.raw.gtsr1,
                R.raw.gtsr2,
                R.raw.gtsr3,
                R.raw.gtsr4,
                R.raw.isrg_root_x1,
                R.raw.isrg_root_x2,
            )
            val connectionSpecs = listOf(
                ConnectionSpec.COMPATIBLE_TLS,
                ConnectionSpec.CLEARTEXT
            )
            SslCompat(context, rawCertResources, connectionSpecs)
        }

        instance<Moshi> {
            Moshi.Builder().build()
        }

        single<DataErrorMapper>()

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
        singleImpl<RemoteFileHolder, RemoteFileStorage>()

        singleImpl<CookieHolder, CookiesStorage>()
        singleImpl<UserHolder, UserStorage>()
        singleImpl<AuthHolder, AuthStorage>()

        single<ApiConfigChanger>()

        single<AppCookieJar>()
        single<UnauthorizedInterceptor>()
        single<AppInfoInterceptor>()
        single<UserAgentGenerator>()
        single<ApiConfig>()
        single<ApiConfigStorage>()



        single<CheckerApiDataSource>()
        single<ConfigurationApiDataSource>()
        single<MenuApiDataSource>()
        single<DonationApiDataSource>()

        single<AuthRepository>()
        single<ReleaseRepository>()
        single<ConfigurationRepository>()
        single<SearchRepository>()
        single<VkCommentsRepository>()
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
        single<SslCompatAnalytics>()
        single<AnalyticsMainProfileDataSource>()
        single<AnalyticsInstallerProfileDataSource>()
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

        /* Player */
        singleProvider<OkHttpClient, PlayerOkHttpProvider>(PlayerClient::class)
        single<PlayerDataSourceProvider>()
        single<PlayerCacheDataSourceProvider>()

        /* Direct */
        singleProvider<OkHttpClient, DirectOkHttpProvider>(DirectClient::class)
        singleProvider<Retrofit, DirectRetrofitProvider>(DirectRetrofit::class)
        singleProvider<DirectApi, DirectApiProvider>()

        /* Api next */

        single<ApiErrorParser>()

        singleProvider<OkHttpClient, ApiOkhttpProvider>(ApiClient::class)
        singleProvider<Retrofit, ApiRetrofitProvider>(ApiRetrofit::class)

        single<AuthTokenStorage>()
        single<AuthTokenInterceptor>()


        singleProvider<AuthApi, AuthApiProvider>()
        singleProvider<CatalogApi, CatalogApiProvider>()
        singleProvider<CollectionsApi, CollectionsApiProvider>()
        singleProvider<FavoritesApi, FavoritesApiProvider>()
        singleProvider<FranchisesApi, FranchisesApiProvider>()
        singleProvider<GenresApi, GenresApiProvider>()
        singleProvider<ProfileApi, ProfileApiProvider>()
        singleProvider<ReleasesApi, ReleasesApiProvider>()
        singleProvider<ScheduleApi, ScheduleApiProvider>()
        singleProvider<TeamsApi, TeamsApiProvider>()
        singleProvider<TimeCodesApi, TimeCodesApiProvider>()
        singleProvider<VideosApi, VideosApiProvider>()
        singleProvider<TorrentsApi, TorrentsApiProvider>()

        single<AuthApiDataSource>()
        single<ProfileApiDataSource>()
        single<CatalogApiDataSource>()
        single<CollectionsApiDataSource>()
        single<FavoritesApiDataSource>()
        single<FranchisesApiDataSource>()
        single<ReleasesApiDataSource>()
        single<ScheduleApiDataSource>()
        single<TeamsApiDataSource>()
        single<TimeCodesApiDataSource>()
        single<VideosApiDataSource>()
        single<TorrentsApiDataSource>()

        single<FranchisesRepository>()
        single<CatalogRepository>()
        single<CollectionsRepository>()

        single<FavoritesInteractor>()
        single<CollectionsInteractor>()
        single<FilterInteractor>()
    }


    class DirectRetrofitProvider @Inject constructor(
        @DirectClient private val okHttpClient: OkHttpClient,
    ) : Provider<Retrofit> {
        override fun get(): Retrofit {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://anilibria.top/api/v1/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
            return retrofit
        }
    }

    class DirectApiProvider @Inject constructor(
        @DirectRetrofit private val retrofit: Retrofit
    ) : Provider<DirectApi> {
        override fun get(): DirectApi = retrofit.create()
    }


    class ApiRetrofitProvider @Inject constructor(
        @ApiClient private val okHttpClient: OkHttpClient,
    ) : Provider<Retrofit> {
        override fun get(): Retrofit {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://anilibria.top/api/v1/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
            return retrofit
        }
    }

    class AuthApiProvider @Inject constructor(
        @ApiRetrofit private val retrofit: Retrofit
    ) : Provider<AuthApi> {
        override fun get(): AuthApi = retrofit.create()
    }

    class CatalogApiProvider @Inject constructor(
        @ApiRetrofit private val retrofit: Retrofit
    ) : Provider<CatalogApi> {
        override fun get(): CatalogApi = retrofit.create()
    }

    class CollectionsApiProvider @Inject constructor(
        @ApiRetrofit private val retrofit: Retrofit
    ) : Provider<CollectionsApi> {
        override fun get(): CollectionsApi = retrofit.create()
    }

    class FavoritesApiProvider @Inject constructor(
        @ApiRetrofit private val retrofit: Retrofit
    ) : Provider<FavoritesApi> {
        override fun get(): FavoritesApi = retrofit.create()
    }

    class FranchisesApiProvider @Inject constructor(
        @ApiRetrofit private val retrofit: Retrofit
    ) : Provider<FranchisesApi> {
        override fun get(): FranchisesApi = retrofit.create()
    }

    class GenresApiProvider @Inject constructor(
        @ApiRetrofit private val retrofit: Retrofit
    ) : Provider<GenresApi> {
        override fun get(): GenresApi = retrofit.create()
    }

    class ProfileApiProvider @Inject constructor(
        @ApiRetrofit private val retrofit: Retrofit
    ) : Provider<ProfileApi> {
        override fun get(): ProfileApi = retrofit.create()
    }

    class ReleasesApiProvider @Inject constructor(
        @ApiRetrofit private val retrofit: Retrofit
    ) : Provider<ReleasesApi> {
        override fun get(): ReleasesApi = retrofit.create()
    }

    class ScheduleApiProvider @Inject constructor(
        @ApiRetrofit private val retrofit: Retrofit
    ) : Provider<ScheduleApi> {
        override fun get(): ScheduleApi = retrofit.create()
    }

    class TeamsApiProvider @Inject constructor(
        @ApiRetrofit private val retrofit: Retrofit
    ) : Provider<TeamsApi> {
        override fun get(): TeamsApi = retrofit.create()
    }

    class TimeCodesApiProvider @Inject constructor(
        @ApiRetrofit private val retrofit: Retrofit
    ) : Provider<TimeCodesApi> {
        override fun get(): TimeCodesApi = retrofit.create()
    }

    class VideosApiProvider @Inject constructor(
        @ApiRetrofit private val retrofit: Retrofit
    ) : Provider<VideosApi> {
        override fun get(): VideosApi = retrofit.create()
    }

    class TorrentsApiProvider @Inject constructor(
        @ApiRetrofit private val retrofit: Retrofit
    ) : Provider<TorrentsApi> {
        override fun get(): TorrentsApi = retrofit.create()
    }

    class PreferencesProvider @Inject constructor(
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

    class DataPreferencesProvider @Inject constructor(
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