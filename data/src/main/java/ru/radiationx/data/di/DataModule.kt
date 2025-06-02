package ru.radiationx.data.di

import android.content.Context
import android.content.SharedPreferences
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
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import ru.radiationx.data.R
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
import ru.radiationx.data.api.auth.AuthApiDataSource
import ru.radiationx.data.api.auth.AuthHolder
import ru.radiationx.data.api.auth.AuthRepository
import ru.radiationx.data.api.auth.AuthStorage
import ru.radiationx.data.api.auth.AuthTokenStorage
import ru.radiationx.data.api.auth.legacy.AppCookieJar
import ru.radiationx.data.api.auth.legacy.CookieHolder
import ru.radiationx.data.api.auth.legacy.CookiesStorage
import ru.radiationx.data.api.catalog.CatalogApiDataSource
import ru.radiationx.data.api.catalog.CatalogRepository
import ru.radiationx.data.api.collections.CollectionsApiDataSource
import ru.radiationx.data.api.collections.CollectionsInteractor
import ru.radiationx.data.api.collections.CollectionsRepository
import ru.radiationx.data.api.favorites.FavoriteRepository
import ru.radiationx.data.api.favorites.FavoritesApiDataSource
import ru.radiationx.data.api.favorites.FavoritesInteractor
import ru.radiationx.data.api.franchises.FranchisesApiDataSource
import ru.radiationx.data.api.franchises.FranchisesRepository
import ru.radiationx.data.api.genres.GenresApiDataSource
import ru.radiationx.data.api.profile.ProfileApiDataSource
import ru.radiationx.data.api.profile.UserHolder
import ru.radiationx.data.api.profile.UserStorage
import ru.radiationx.data.api.releases.ReleaseInteractor
import ru.radiationx.data.api.releases.ReleaseRepository
import ru.radiationx.data.api.releases.ReleasesApiDataSource
import ru.radiationx.data.api.schedule.ScheduleApiDataSource
import ru.radiationx.data.api.schedule.ScheduleRepository
import ru.radiationx.data.api.shared.filter.FilterInteractor
import ru.radiationx.data.api.shared.filter.legacy.SearchRepository
import ru.radiationx.data.api.teams.TeamsApiDataSource
import ru.radiationx.data.api.teams.TeamsRepository
import ru.radiationx.data.api.timecodes.TimeCodesApiDataSource
import ru.radiationx.data.api.torrents.TorrentsApiDataSource
import ru.radiationx.data.api.videos.VideosApiDataSource
import ru.radiationx.data.api.videos.YoutubeRepository
import ru.radiationx.data.app.DirectApi
import ru.radiationx.data.app.ads.AdsConfigApiDataSource
import ru.radiationx.data.app.ads.AdsConfigRepository
import ru.radiationx.data.app.ads.AdsConfigStorage
import ru.radiationx.data.app.config.ApiConfig
import ru.radiationx.data.app.config.ApiConfigChanger
import ru.radiationx.data.app.config.ApiConfigStorage
import ru.radiationx.data.app.config.ConfigurationApiDataSource
import ru.radiationx.data.app.config.ConfigurationRepository
import ru.radiationx.data.app.donation.DonationApiDataSource
import ru.radiationx.data.app.donation.DonationHolder
import ru.radiationx.data.app.donation.DonationRepository
import ru.radiationx.data.app.donation.DonationStorage
import ru.radiationx.data.app.downloader.RemoteFileHolder
import ru.radiationx.data.app.downloader.RemoteFileRepository
import ru.radiationx.data.app.downloader.RemoteFileStorage
import ru.radiationx.data.app.episodeaccess.EpisodesCheckerHolder
import ru.radiationx.data.app.episodeaccess.EpisodesCheckerStorage
import ru.radiationx.data.app.feed.FeedRepository
import ru.radiationx.data.app.history.HistoryHolder
import ru.radiationx.data.app.history.HistoryRepository
import ru.radiationx.data.app.history.HistoryRuntimeCache
import ru.radiationx.data.app.history.HistoryStorage
import ru.radiationx.data.app.menu.MenuApiDataSource
import ru.radiationx.data.app.menu.MenuHolder
import ru.radiationx.data.app.menu.MenuRepository
import ru.radiationx.data.app.menu.MenuStorage
import ru.radiationx.data.app.preferences.PreferencesHolder
import ru.radiationx.data.app.preferences.PreferencesStorage
import ru.radiationx.data.app.releaseupdate.ReleaseUpdateHolder
import ru.radiationx.data.app.releaseupdate.ReleaseUpdateMiddleware
import ru.radiationx.data.app.releaseupdate.ReleaseUpdateStorage
import ru.radiationx.data.app.updater.CheckerApiDataSource
import ru.radiationx.data.app.updater.CheckerRepository
import ru.radiationx.data.app.vkcomments.VkCommentsRepository
import ru.radiationx.data.di.providers.ApiOkhttpProvider
import ru.radiationx.data.di.providers.ApiRetrofitProvider
import ru.radiationx.data.di.providers.AuthApiProvider
import ru.radiationx.data.di.providers.CatalogApiProvider
import ru.radiationx.data.di.providers.CollectionsApiProvider
import ru.radiationx.data.di.providers.DataPreferencesProvider
import ru.radiationx.data.di.providers.DirectApiProvider
import ru.radiationx.data.di.providers.DirectOkHttpProvider
import ru.radiationx.data.di.providers.DirectRetrofitProvider
import ru.radiationx.data.di.providers.FavoritesApiProvider
import ru.radiationx.data.di.providers.FranchisesApiProvider
import ru.radiationx.data.di.providers.GenresApiProvider
import ru.radiationx.data.di.providers.PlayerOkHttpProvider
import ru.radiationx.data.di.providers.PreferencesProvider
import ru.radiationx.data.di.providers.ProfileApiProvider
import ru.radiationx.data.di.providers.ReleasesApiProvider
import ru.radiationx.data.di.providers.ScheduleApiProvider
import ru.radiationx.data.di.providers.TeamsApiProvider
import ru.radiationx.data.di.providers.TimeCodesApiProvider
import ru.radiationx.data.di.providers.TorrentsApiProvider
import ru.radiationx.data.di.providers.VideosApiProvider
import ru.radiationx.data.app.versions.AppVersionsDataSource
import ru.radiationx.data.app.versions.AppVersionsDataSourceImpl
import ru.radiationx.data.network.DataErrorMapper
import ru.radiationx.data.network.UserAgentGenerator
import ru.radiationx.data.network.interceptors.AppInfoInterceptor
import ru.radiationx.data.network.interceptors.AuthTokenInterceptor
import ru.radiationx.data.network.interceptors.UnauthorizedInterceptor
import ru.radiationx.data.network.sslcompat.SslCompat
import ru.radiationx.data.player.PlayerCacheDataSourceProvider
import ru.radiationx.data.player.PlayerDataSourceProvider
import ru.radiationx.quill.QuillModule

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

        singleImpl<AppVersionsDataSource, AppVersionsDataSourceImpl>()

        single<PreferencesStorage>()

        singleImpl<PreferencesHolder, PreferencesStorage>()
        singleImpl<EpisodesCheckerHolder, EpisodesCheckerStorage>()
        singleImpl<HistoryHolder, HistoryStorage>()
        singleImpl<ReleaseUpdateHolder, ReleaseUpdateStorage>()
        singleImpl<MenuHolder, MenuStorage>()
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
        single<AdsConfigApiDataSource>()
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
        single<GenresApiDataSource>()
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


}