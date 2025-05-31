package anilibria.api.torrent

import anilibria.api.shared.PaginationResponse
import anilibria.api.torrent.models.TorrentResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TorrentsApi {

    @GET("anime/torrents")
    suspend fun getTorrents(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?
    ): PaginationResponse<TorrentResponse>

    @GET("anime/torrents/{hashOrId}")
    suspend fun getTorrent(
        @Path("hashOrId") hashOrId: String
    ): TorrentResponse

    @GET("anime/torrents/{hashOrId}/file")
    suspend fun getTorrentFile(
        @Path("hashOrId") hashOrId: String,
        @Query("pk") pk: String?
    ): ResponseBody

    @GET("anime/torrents/release/{releaseId}")
    suspend fun getTorrentsByRelease(
        @Path("releaseId") releaseId: Int
    ): List<TorrentResponse>

}