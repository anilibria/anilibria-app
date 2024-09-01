package ru.radiationx.data.apinext.datasources

import anilibria.api.franchises.FranchisesApi
import ru.radiationx.data.apinext.toDomain
import ru.radiationx.data.entity.domain.release.Franchise
import ru.radiationx.data.entity.domain.types.FranchiseId
import ru.radiationx.data.entity.domain.types.ReleaseId

class FranchisesApiDataSource(
    private val api: FranchisesApi
) {

    suspend fun getFranchises(): List<Franchise> {
        return api.getFranchises().map { it.toDomain() }
    }

    suspend fun getFranchise(franchiseId: FranchiseId): List<Franchise> {
        return api.getFranchise(franchiseId.id).map { it.toDomain() }
    }

    suspend fun getRandomFranchises(limit: Int?): List<Franchise> {
        return api.getRandomFranchises(limit).map { it.toDomain() }
    }

    suspend fun getReleaseFranchises(id: ReleaseId): List<Franchise> {
        return api.getReleaseFranchises(id.id.toString()).map { it.toDomain() }
    }
}