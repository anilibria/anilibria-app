package ru.radiationx.data.di.providers

import anilibria.api.auth.AuthApi
import anilibria.api.catalog.CatalogApi
import anilibria.api.collections.CollectionsApi
import anilibria.api.favorites.FavoritesApi
import anilibria.api.franchises.FranchisesApi
import anilibria.api.genres.GenresApi
import anilibria.api.profile.ProfileApi
import anilibria.api.releases.ReleasesApi
import anilibria.api.schedule.ScheduleApi
import anilibria.api.teams.TeamsApi
import anilibria.api.timecodes.TimeCodesApi
import anilibria.api.torrent.TorrentsApi
import anilibria.api.videos.VideosApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import ru.radiationx.data.di.ApiClient
import ru.radiationx.data.di.ApiRetrofit
import javax.inject.Inject
import javax.inject.Provider


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