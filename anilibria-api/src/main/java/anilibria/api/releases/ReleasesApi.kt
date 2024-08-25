package anilibria.api.releases

import anilibria.api.shared.release.ReleaseEpisodeResponse
import anilibria.api.shared.release.ReleaseMemberResponse
import anilibria.api.shared.release.ReleaseResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReleasesApi {

    @GET("anime/releases/latest")
    suspend fun getLatestReleases(@Query("limit") limit: Int?): List<ReleaseResponse>

    @GET("anime/releases/random")
    suspend fun getRandomReleases(@Query("limit") limit: Int?): List<ReleaseResponse>

    @GET("anime/releases/{aliasOrId}")
    suspend fun getRelease(@Path("aliasOrId") aliasOrId: String): ReleaseResponse

    @GET("anime/releases/{aliasOrId}/members")
    suspend fun getMembers(@Path("aliasOrId") aliasOrId: String): List<ReleaseMemberResponse>

    @GET("anime/releases/episodes/{releaseEpisodeId}")
    suspend fun getEpisode(@Path("releaseEpisodeId") releaseEpisodeId: String): ReleaseEpisodeResponse

    @GET("app/search/releases")
    suspend fun search(@Query("query") query: String): List<ReleaseResponse>
}