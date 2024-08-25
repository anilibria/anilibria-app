package anilibria.api.franchises

import anilibria.api.franchises.models.FranchiseResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FranchisesApi {

    @GET("anime/franchises")
    suspend fun getFranchises(): List<FranchiseResponse>

    @GET("anime/franchises/{franchiseId}")
    suspend fun getFranchise(@Path("franchiseId") franchiseId: String): List<FranchiseResponse>

    @GET("anime/franchises/random")
    suspend fun getRandomFranchises(@Query("limit") limit: Int?): List<FranchiseResponse>

    @GET("anime/franchises/release/{releaseId}")
    suspend fun getReleaseFranchises(@Path("releaseId") releaseId: String): List<FranchiseResponse>
}