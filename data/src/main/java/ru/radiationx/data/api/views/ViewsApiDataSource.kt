package ru.radiationx.data.api.views

import anilibria.api.views.ViewsApi
import anilibria.api.views.models.TimeCodeDeleteRequest
import anilibria.api.views.models.TimeCodeNetwork
import ru.radiationx.data.api.shared.pagination.Paginated
import ru.radiationx.data.api.shared.pagination.toDomain
import ru.radiationx.data.api.shared.toApiDate
import ru.radiationx.data.api.views.mapper.toDomain
import ru.radiationx.data.api.views.mapper.toNetwork
import ru.radiationx.data.api.views.models.TimeCode
import ru.radiationx.data.api.views.models.ViewHistory
import ru.radiationx.data.common.EpisodeUUID
import java.util.Date
import javax.inject.Inject

class ViewsApiDataSource @Inject constructor(
    private val api: ViewsApi
) {

    suspend fun getHistory(page: Int?, limit: Int?): Paginated<ViewHistory> {
        return api.getHistory(page, limit).toDomain {
            it.toDomain()
        }
    }

    suspend fun getTimeCodes(since: Date?): List<TimeCode> {
        return api.getTimeCodes(since?.toApiDate()).toDomain()
    }

    suspend fun updateTimeCodes(timeCodes: List<TimeCode>) {
        val request = timeCodes.map { it.toNetwork() }
        return api.updateTimeCodes(request)
    }

    suspend fun deleteTimeCodes(ids: List<EpisodeUUID>) {
        val request = ids.map { TimeCodeDeleteRequest(it.uuid) }
        return api.deleteTimeCodes(request)
    }

    private fun List<List<Any>>.toDomain(): List<TimeCode> {
        return map {
            TimeCodeNetwork.ofList(it).toDomain()
        }
    }
}