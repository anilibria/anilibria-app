package anilibria.api.franchises

import anilibria.api.franchises.models.FranchiseResponse
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface FranchisesApi {

    @GET("/anime/franchises")
    suspend fun getFranchises(): List<FranchiseResponse>

    @GET("/anime/franchises/{franchiseId}")
    suspend fun getFranchise(@Path("franchiseId") franchiseId: String): List<FranchiseResponse>

    @GET("/anime/franchises/random")
    suspend fun getRandomFranchises(@Query("limit") limit: Int?): List<FranchiseResponse>

    @GET("/anime/franchises/release/{releaseId}")
    suspend fun getReleaseFranchises(@Path("releaseId") releaseId: String): List<FranchiseResponse>
}