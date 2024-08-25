package anilibria.api.catalog

import anilibria.api.shared.PaginationResponse
import anilibria.api.shared.filter.FilterAgeRatingResponse
import anilibria.api.shared.filter.FilterGenreResponse
import anilibria.api.shared.filter.FilterProductionStatusResponse
import anilibria.api.shared.filter.FilterPublishStatusResponse
import anilibria.api.shared.filter.FilterReleaseTypeResponse
import anilibria.api.shared.filter.FilterSeasonResponse
import anilibria.api.shared.filter.FilterSortingResponse
import anilibria.api.shared.release.ReleaseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CatalogApi {

    @GET("anime/catalog/references/age-ratings")
    suspend fun getAgeRatings(): List<FilterAgeRatingResponse>

    @GET("anime/catalog/references/genres")
    suspend fun getGenres(): List<FilterGenreResponse>

    @GET("anime/catalog/references/production-statuses")
    suspend fun getProductionStatuses(): List<FilterProductionStatusResponse>

    @GET("anime/catalog/references/publish-statuses")
    suspend fun getPublishStatuses(): List<FilterPublishStatusResponse>

    @GET("anime/catalog/references/seasons")
    suspend fun getSeasons(): List<FilterSeasonResponse>

    @GET("anime/catalog/references/sorting")
    suspend fun getSortings(): List<FilterSortingResponse>

    @GET("anime/catalog/references/types")
    suspend fun getTypes(): List<FilterReleaseTypeResponse>

    @GET("anime/catalog/references/years")
    suspend fun getYears(): List<Int>

    @GET("anime/catalog/releases")
    suspend fun getReleases(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("f[genres]") genres: String?,
        @Query("f[types]") types: String?,
        @Query("f[seasons]") seasons: String?,
        @Query("f[years][from_year]") fromYear: String?,
        @Query("f[years][to_year]") toYear: String?,
        @Query("f[search]") search: String?,
        @Query("f[sorting]") sorting: String?,
        @Query("f[age_ratings]") ageRatings: String?,
        @Query("f[publish_statuses]") publishStatuses: String?,
        @Query("f[production_statuses]") productionStatuses: String?,
    ): PaginationResponse<ReleaseResponse>

    // TODO implement from actual api
    /*@POST("anime/catalog/releases")
    suspend fun getReleases(): Unit*/
}