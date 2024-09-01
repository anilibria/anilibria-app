package anilibria.api.collections

import anilibria.api.shared.filter.FilterAgeRatingResponse
import anilibria.api.shared.CollectionReleaseIdNetwork
import anilibria.api.shared.filter.FilterReleaseTypeResponse
import anilibria.api.shared.filter.FilterGenreResponse
import anilibria.api.shared.PaginationResponse
import anilibria.api.shared.release.ReleaseResponse
import anilibria.api.shared.ReleaseIdNetwork
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CollectionsApi {

    @GET("accounts/users/me/collections/references/age-ratings")
    suspend fun getAgeRatings(): List<FilterAgeRatingResponse>

    @GET("accounts/users/me/collections/references/genres")
    suspend fun getGenres(): List<FilterGenreResponse>

    @GET("accounts/users/me/collections/references/types")
    suspend fun getTypes(): List<FilterReleaseTypeResponse>

    @GET("accounts/users/me/collections/references/years")
    suspend fun getYears(): List<Int>

    @GET("accounts/users/me/collections/ids")
    suspend fun getIds(): List<CollectionReleaseIdNetwork>

    @GET("accounts/users/me/collections/releases")
    suspend fun getReleases(
        @Query("type_of_collection") collectionType: String,
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("f[genres]") genres: String?,
        @Query("f[types]") types: String?,
        @Query("f[years]") years: String?,
        @Query("f[search]") search: String?,
        @Query("f[age_ratings]") ageRatings: String?
    ): PaginationResponse<ReleaseResponse>

    // TODO implement from actual api
    /*@POST("accounts/users/me/collections/releases")
    suspend fun getReleases(): Unit*/

    @POST("accounts/users/me/collections")
    suspend fun addReleases(@Body body: List<CollectionReleaseIdNetwork>)

    @DELETE("accounts/users/me/collections")
    suspend fun deleteReleases(@Body body: List<ReleaseIdNetwork>)
}