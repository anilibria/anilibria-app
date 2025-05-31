package anilibria.api.collections

import anilibria.api.collections.models.CollectionsRequest
import anilibria.api.shared.CollectionReleaseIdNetwork
import anilibria.api.shared.PaginationResponse
import anilibria.api.shared.ReleaseIdNetwork
import anilibria.api.shared.filter.FilterAgeRatingResponse
import anilibria.api.shared.filter.FilterGenreResponse
import anilibria.api.shared.filter.FilterReleaseTypeResponse
import anilibria.api.shared.release.ReleaseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
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

    /*
    * [0] - release_id: Int
    * [1] - type_of_collection: String
    * */
    @GET("accounts/users/me/collections/ids")
    suspend fun getIds(): List<List<Any>>

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

    @POST("accounts/users/me/collections/releases")
    suspend fun getReleases(@Body body: CollectionsRequest): PaginationResponse<ReleaseResponse>

    @POST("accounts/users/me/collections")
    suspend fun addReleases(@Body body: List<CollectionReleaseIdNetwork>)

    @HTTP(method = "DELETE", path = "accounts/users/me/collections", hasBody = true)
    suspend fun deleteReleases(@Body body: List<ReleaseIdNetwork>)
}