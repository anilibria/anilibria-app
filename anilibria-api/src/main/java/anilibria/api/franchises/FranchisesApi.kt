package anilibria.api.franchises

import anilibria.api.franchises.models.FranchiseResponse
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path

interface FranchisesApi {

    @GET("/anime/franchises")
    suspend fun getFranchises(): List<FranchiseResponse>

    @GET("/anime/franchises/{franchiseId}")
    suspend fun getFranchise(@Path("franchiseId") franchiseId: String): List<FranchiseResponse>

    @GET("/anime/franchises/random")
    suspend fun getRandomFranchise(): List<FranchiseResponse>

    @GET("/anime/franchises/release/{releaseId}")
    suspend fun getReleaseFranchises(@Path("releaseId") releaseId: String): List<FranchiseResponse>
}