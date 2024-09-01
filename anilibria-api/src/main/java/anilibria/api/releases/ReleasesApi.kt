package anilibria.api.releases

import anilibria.api.shared.release.ReleaseEpisodeResponse
import anilibria.api.shared.release.ReleaseMemberResponse
import anilibria.api.shared.release.ReleaseResponse
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.radiationx.shared.ktx.coRunCatching

interface ReleasesApi {

    @GET("/anime/releases/latest")
    suspend fun getLatestReleases(@Query("limit") limit: Int?): List<ReleaseResponse>

    @GET("/anime/releases/random")
    suspend fun getRandomReleases(@Query("limit") limit: Int?): List<ReleaseResponse>

    @GET("/anime/releases/{aliasOrId}")
    suspend fun getRelease(@Path("aliasOrId") aliasOrId: String): ReleaseResponse

    @GET("/anime/releases/{aliasOrId}/members")
    suspend fun getMembers(@Path("aliasOrId") aliasOrId: String): List<ReleaseMemberResponse>

    @GET("/anime/releases/episodes/{releaseEpisodeId}")
    suspend fun getEpisode(@Path("releaseEpisodeId") releaseEpisodeId: String): ReleaseEpisodeResponse

    @GET("/app/search/releases")
    suspend fun search(@Query("query") query: String): List<ReleaseResponse>

    suspend fun getReleasesByIds(ids: List<Int>): List<ReleaseResponse> {
        return coroutineScope {
            val requests = async {
                ids.map { id ->
                    coRunCatching { getRelease(id.toString()) }
                }
            }
            requests.await().mapNotNull { it.getOrNull() }
        }
    }
}