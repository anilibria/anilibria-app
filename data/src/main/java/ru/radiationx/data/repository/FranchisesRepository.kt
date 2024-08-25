package ru.radiationx.data.repository

import ru.radiationx.data.apinext.datasources.FranchisesApiDataSource
import ru.radiationx.data.entity.domain.release.Franchise
import ru.radiationx.data.entity.domain.release.FranchiseFull
import ru.radiationx.data.entity.domain.types.FranchiseId
import ru.radiationx.data.entity.domain.types.ReleaseId
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