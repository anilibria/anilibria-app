package anilibria.api.catalog

import anilibria.api.shared.filter.FilterAgeRatingResponse
import anilibria.api.shared.CollectionReleaseIdNetwork
import anilibria.api.shared.filter.FilterCollectionTypeResponse
import anilibria.api.shared.filter.FilterGenreResponse
import anilibria.api.shared.PaginationResponse
import anilibria.api.shared.ReleaseResponse
import anilibria.api.shared.filter.FilterProductionsStatusResponse
import anilibria.api.shared.filter.FilterPublishStatusResponse
import anilibria.api.shared.filter.FilterSeasonResponse
import anilibria.api.shared.filter.FilterSortingResponse
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query

interface CatalogApi {

    @GET("/anime/catalog/references/age-ratings")
    suspend fun getAgeRatings(): List<FilterAgeRatingResponse>

    @GET("/anime/catalog/references/genres")
    suspend fun getGenres(): List<FilterGenreResponse>

    @GET("/anime/catalog/references/production-statuses")
    suspend fun getProductionStatuses(): List<FilterProductionsStatusResponse>

    @GET("/anime/catalog/references/publish-statuses")
    suspend fun getPublishStatuses(): List<FilterPublishStatusResponse>

    @GET("/anime/catalog/references/seasons")
    suspend fun getSeasons(): List<FilterSeasonResponse>

    @GET("/anime/catalog/references/sorting")
    suspend fun getSortings(): List<FilterSortingResponse>

    @GET("/anime/catalog/references/types")
    suspend fun getTypes(): List<FilterCollectionTypeResponse>

    @GET("/anime/catalog/references/years")
    suspend fun getYears(): List<Int>

    @GET("/anime/catalog/ids")
    suspend fun getIds(): List<CollectionReleaseIdNetwork>

    @GET("/anime/catalog/releases")
    suspend fun getReleases(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("f[genres]") genres: String?,
        @Query("f[types]") types: List<String>?,
        @Query("f[seasons]") seasons: List<String>?,
        @Query("f[years][from_year]") fromYear: String?,
        @Query("f[years][to_year]") toYear: String?,
        @Query("f[search]") search: String?,
        @Query("f[sorting]") sorting: String?,
        @Query("f[age_ratings]") ageRatings: List<String>?,
        @Query("f[publish_statuses]") publishStatuses: List<String>?,
        @Query("f[production_statuses]") productionStatuses: List<String>?,
    ): PaginationResponse<ReleaseResponse>

    // TODO implement from actual api
    /*@POST("/anime/catalog/releases")
    suspend fun getReleases(): Unit*/
}