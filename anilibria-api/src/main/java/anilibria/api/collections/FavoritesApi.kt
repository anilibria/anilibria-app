package anilibria.api.collections

import anilibria.api.shared.AgeRatingResponse
import anilibria.api.shared.CollectionReleaseIdNetwork
import anilibria.api.shared.CollectionTypeResponse
import anilibria.api.shared.GenreResponse
import anilibria.api.shared.PaginationResponse
import anilibria.api.shared.ReleaseResponse
import anilibria.api.shared.ReleaseIdNetwork
import anilibria.api.shared.SortingResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query

interface FavoritesApi {

    @GET("/accounts/users/me/favorites/references/age-ratings")
    suspend fun getAgeRatings(): List<AgeRatingResponse>

    @GET("/accounts/users/me/favorites/references/genres")
    suspend fun getGenres(): List<GenreResponse>

    @GET("/accounts/users/me/favorites/references/sorting")
    suspend fun getSorting(): List<SortingResponse>

    @GET("/accounts/users/me/favorites/references/types")
    suspend fun getTypes(): List<CollectionTypeResponse>

    @GET("/accounts/users/me/favorites/references/years")
    suspend fun getYears(): List<Int>

    @GET("/accounts/users/me/favorites/ids")
    suspend fun getIds(): List<Int>

    @GET("/accounts/users/me/favorites/releases")
    suspend fun getReleases(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("f[years]") years: String?,
        @Query("f[types]") types: List<String>?,
        @Query("f[genres]") genres: String?,
        @Query("f[search]") search: String?,
        @Query("f[sorting]") sorting: String?,
        @Query("f[age_ratings]") ageRatings: List<String>?
    ): PaginationResponse<ReleaseResponse>

    // TODO implement from actual api
    /*@POST("/accounts/users/me/favorites/releases")
    suspend fun getReleases(): Unit*/

    @POST("/accounts/users/me/favorites")
    suspend fun addReleases(@Body body: List<ReleaseIdNetwork>)

    @DELETE("/accounts/users/me/favorites")
    suspend fun deleteReleases(@Body body: List<ReleaseIdNetwork>)
}