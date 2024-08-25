package anilibria.api.genres

import anilibria.api.genres.models.GenreResponse
import anilibria.api.shared.PaginationResponse
import anilibria.api.shared.release.ReleaseResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GenresApi {

    @GET("anime/genres")
    suspend fun getGenres(): List<GenreResponse>

    @GET("anime/genres/{genreId}")
    suspend fun getGenre(@Path("genreId") genreId: String): GenreResponse

    @GET("anime/genres/random")
    suspend fun getRandomGenres(@Query("limit") limit: Int?): List<GenreResponse>

    @GET("anime/genres/{genreId}/releases")
    suspend fun getGenreReleases(@Path("genreId") genreId: String): PaginationResponse<ReleaseResponse>
}