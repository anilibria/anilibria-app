package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.domain.release.Franchise
import ru.radiationx.data.entity.domain.release.FranchiseInfo
import ru.radiationx.data.entity.domain.release.FranchiseRelease
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.response.release.FranchiseInfoResponse
import ru.radiationx.data.entity.response.release.FranchiseReleaseResponse
import ru.radiationx.data.entity.response.release.FranchiseResponse

fun FranchiseResponse.toDomain() = Franchise(
    info = info.toDomain(),
    releases = releases.map { it.toDomain() }
)

fun FranchiseInfoResponse.toDomain() = FranchiseInfo(
    id = id,
    name = name
)

fun FranchiseReleaseResponse.toDomain() = FranchiseRelease(
    releaseId = ReleaseId(id),
    names = listOfNotNull(name, ename, aname),
    code = ReleaseCode(alias)
)