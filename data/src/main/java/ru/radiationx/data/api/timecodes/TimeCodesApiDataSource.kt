package ru.radiationx.data.api.timecodes

import anilibria.api.timecodes.TimeCodesApi
import anilibria.api.timecodes.models.TimeCodeDeleteRequest
import anilibria.api.timecodes.models.TimeCodeNetwork
import ru.radiationx.data.api.timecodes.mapper.toDomain
import ru.radiationx.data.api.timecodes.mapper.toNetwork
import ru.radiationx.data.api.timecodes.models.TimeCode
import ru.radiationx.data.common.EpisodeUUID
import javax.inject.Inject

class TimeCodesApiDataSource @Inject constructor(
    private val api: TimeCodesApi
) {

    suspend fun get(): List<TimeCode> {
        return api.get().toDomain()
    }

    suspend fun update(timeCodes: List<TimeCode>): List<TimeCode> {
        val request = timeCodes.map { it.toNetwork() }
        return api.update(request).toDomain()
    }

    suspend fun delete(ids: List<EpisodeUUID>): List<TimeCode> {
        val request = ids.map { TimeCodeDeleteRequest(it.uuid) }
        return api.delete(request).toDomain()
    }

    private fun List<List<Any>>.toDomain(): List<TimeCode> {
        return map {
            TimeCodeNetwork.ofList(it).toDomain()
        }
    }
}