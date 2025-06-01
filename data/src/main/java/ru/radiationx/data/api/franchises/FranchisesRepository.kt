package ru.radiationx.data.api.franchises

import ru.radiationx.data.api.franchises.models.Franchise
import ru.radiationx.data.api.franchises.models.FranchiseFull
import ru.radiationx.data.common.FranchiseId
import ru.radiationx.data.common.ReleaseId
import toothpick.InjectConstructor

@InjectConstructor
class FranchisesRepository(
    private val franchisesApi: FranchisesApiDataSource
) {

    suspend fun getFranchises(): List<Franchise> {
        return franchisesApi.getFranchises()
    }

    suspend fun getFranchise(franchiseId: FranchiseId): List<FranchiseFull> {
        return franchisesApi.getFranchise(franchiseId)
    }

    suspend fun getRandomFranchises(limit: Int?): List<Franchise> {
        return franchisesApi.getRandomFranchises(limit)
    }

    suspend fun getReleaseFranchises(id: ReleaseId): List<FranchiseFull> {
        return franchisesApi.getReleaseFranchises(id)
    }
}