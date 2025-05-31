package ru.radiationx.data.apinext.datasources

import anilibria.api.timecodes.TimeCodesApi
import anilibria.api.timecodes.models.TimeCodeDeleteRequest
import anilibria.api.timecodes.models.TimeCodeNetwork
import ru.radiationx.data.apinext.models.TimeCode
import ru.radiationx.data.apinext.toDomain
import ru.radiationx.data.apinext.toNetwork
import ru.radiationx.data.entity.domain.types.EpisodeUUID
import toothpick.InjectConstructor

@InjectConstructor
class TimeCodesApiDataSource(
    private val api: TimeCodesApi
) {

    suspend fun get(): List<TimeCode> {
        return api.get().map {
            TimeCodeNetwork.ofList(it).toDomain()
        }
    }

    suspend fun update(timeCodes: List<TimeCode>) {
        val request = timeCodes.map { it.toNetwork() }
        api.update(request)
    }

    suspend fun delete(ids: List<EpisodeUUID>) {
        val request = ids.map { TimeCodeDeleteRequest(it.uuid) }
        api.delete(request)
    }
}