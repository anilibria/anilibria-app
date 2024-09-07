package anilibria.api.favorites

import anilibria.api.shared.filter.FilterAgeRatingResponse
import anilibria.api.shared.filter.FilterReleaseTypeResponse
import anilibria.api.shared.filter.FilterGenreResponse
import anilibria.api.shared.PaginationResponse
import anilibria.api.shared.release.ReleaseResponse
import anilibria.api.shared.ReleaseIdNetwork
import anilibria.api.shared.filter.FilterSortingResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Query

interface FavoritesApi {

    @GET("accounts/users/me/favorites/references/age-ratings")
    suspend fun getAgeRatings(): List<FilterAgeRatingResponse>

    @GET("accounts/users/me/favorites/references/genres")
    suspend fun getGenres(): List<FilterGenreResponse>

    @GET("accounts/users/me/favorites/references/sorting")
    suspend fun getSortings(): List<FilterSortingResponse>

    @GET("accounts/users/me/favorites/references/types")
    suspend fun getTypes(): List<FilterReleaseTypeResponse>

    @GET("accounts/users/me/favorites/references/years")
    suspend fun getYears(): List<Int>

    @GET("accounts/users/me/favorites/ids")
    suspend fun getIds(): List<Int>

    @GET("accounts/users/me/favorites/releases")
    suspend fun getReleases(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("f[years]") years: String?,
        @Query("f[types]") types: String?,
        @Query("f[genres]") genres: String?,
        @Query("f[search]") search: String?,
        @Query("f[sorting]") sorting: String?,
        @Query("f[age_ratings]") ageRatings: String?
    ): PaginationResponse<ReleaseResponse>

    // TODO implement from actual api
    /*@POST("accounts/users/me/favorites/releases")
    suspend fun getReleases(): Unit*/

    @POST("accounts/users/me/favorites")
    suspend fun addReleases(@Body body: List<ReleaseIdNetwork>)

    @HTTP(method = "DELETE", path = "accounts/users/me/favorites", hasBody = true)
    suspend fun deleteReleases(@Body body: List<ReleaseIdNetwork>)
}