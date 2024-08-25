package anilibria.api.collections

import anilibria.api.shared.filter.FilterAgeRatingResponse
import anilibria.api.shared.CollectionReleaseIdNetwork
import anilibria.api.shared.filter.FilterCollectionTypeResponse
import anilibria.api.shared.filter.FilterGenreResponse
import anilibria.api.shared.PaginationResponse
import anilibria.api.shared.release.ReleaseResponse
import anilibria.api.shared.ReleaseIdNetwork
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query

interface CollectionsApi {

    @GET("/accounts/users/me/collections/references/age-ratings")
    suspend fun getAgeRatings(): List<FilterAgeRatingResponse>

    @GET("/accounts/users/me/collections/references/genres")
    suspend fun getGenres(): List<FilterGenreResponse>

    @GET("/accounts/users/me/collections/references/types")
    suspend fun getTypes(): List<FilterCollectionTypeResponse>

    @GET("/accounts/users/me/collections/references/years")
    suspend fun getYears(): List<Int>

    @GET("/accounts/users/me/collections/ids")
    suspend fun getIds(): List<CollectionReleaseIdNetwork>

    @GET("/accounts/users/me/collections/releases")
    suspend fun getReleases(
        @Query("type_of_collection") collectionType: String,
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("f[genres]") genres: String?,
        @Query("f[types]") types: List<String>?,
        @Query("f[years]") years: String?,
        @Query("f[search]") search: String?,
        @Query("f[age_ratings]") ageRatings: List<String>?
    ): PaginationResponse<ReleaseResponse>

    // TODO implement from actual api
    /*@POST("/accounts/users/me/collections/releases")
    suspend fun getReleases(): Unit*/

    @POST("/accounts/users/me/collections")
    suspend fun addReleases(@Body body: List<CollectionReleaseIdNetwork>)

    @DELETE("/accounts/users/me/collections")
    suspend fun deleteReleases(@Body body: List<ReleaseIdNetwork>)
}