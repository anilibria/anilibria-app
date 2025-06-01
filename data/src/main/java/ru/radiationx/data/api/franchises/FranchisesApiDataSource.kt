package ru.radiationx.data.api.franchises

import anilibria.api.franchises.FranchisesApi
import ru.radiationx.data.api.franchises.mapper.toDomain
import ru.radiationx.data.api.franchises.mapper.toDomainFull
import ru.radiationx.data.api.franchises.models.Franchise
import ru.radiationx.data.api.franchises.models.FranchiseFull
import ru.radiationx.data.common.FranchiseId
import ru.radiationx.data.common.ReleaseId
import toothpick.InjectConstructor

@InjectConstructor
class FranchisesApiDataSource(
    private val api: FranchisesApi
) {

    suspend fun getFranchises(): List<Franchise> {
        return api.getFranchises().map { it.toDomain() }
    }

    suspend fun getFranchise(franchiseId: FranchiseId): List<FranchiseFull> {
        return api.getFranchise(franchiseId.id).map { it.toDomainFull() }
    }

    suspend fun getRandomFranchises(limit: Int?): List<Franchise> {
        return api.getRandomFranchises(limit).map { it.toDomain() }
    }

    suspend fun getReleaseFranchises(id: ReleaseId): List<FranchiseFull> {
        return api.getReleaseFranchises(id.id.toString()).map { it.toDomainFull() }
    }
}