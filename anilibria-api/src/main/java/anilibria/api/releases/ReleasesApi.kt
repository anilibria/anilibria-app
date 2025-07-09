package anilibria.api.releases

import anilibria.api.shared.PaginationResponse
import anilibria.api.shared.release.ReleaseEpisodeResponse
import anilibria.api.shared.release.ReleaseMemberResponse
import anilibria.api.shared.release.ReleaseResponse
import anilibria.api.views.models.ViewHistoryResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReleasesApi {

    @GET("anime/releases/latest")
    suspend fun getLatestReleases(@Query("limit") limit: Int?): List<ReleaseResponse>

    @GET("anime/releases/random")
    suspend fun getRandomReleases(@Query("limit") limit: Int?): List<ReleaseResponse>

    @GET("anime/releases/recommended")
    suspend fun getRecommendedReleases(
        @Query("limit") limit: Int?,
        @Query("release_id") releaseId: Int?,
    ): List<ReleaseResponse>

    @GET("anime/releases/list")
    suspend fun getReleases(
        @Query("ids") ids: String?,
        @Query("aliases") aliases: String?,
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
    ): PaginationResponse<ReleaseResponse>

    @GET("anime/releases/{idOrAlias}")
    suspend fun getRelease(@Path("idOrAlias") idOrAlias: String): ReleaseResponse

    @GET("anime/releases/{idOrAlias}/members")
    suspend fun getMembers(@Path("idOrAlias") idOrAlias: String): List<ReleaseMemberResponse>

    @GET("anime/releases/{idOrAlias}/episodes/timecodes")
    suspend fun getReleaseViewHistory(@Path("idOrAlias") idOrAlias: String): List<ViewHistoryResponse>

    @GET("anime/releases/episodes/{releaseEpisodeId}")
    suspend fun getEpisode(@Path("releaseEpisodeId") releaseEpisodeId: String): ReleaseEpisodeResponse

    @GET("anime/releases/episodes/{releaseEpisodeId}/timecode")
    suspend fun getEpisodeViewHistory(@Path("releaseEpisodeId") releaseEpisodeId: String): ViewHistoryResponse

    @GET("app/search/releases")
    suspend fun search(@Query("query") query: String): List<ReleaseResponse>
}