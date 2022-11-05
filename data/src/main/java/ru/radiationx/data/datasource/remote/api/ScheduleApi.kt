package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.datasource.remote.parsers.ReleaseParser
import ru.radiationx.data.datasource.remote.parsers.ScheduleParser
import ru.radiationx.data.entity.app.schedule.ScheduleDay
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.entity.response.schedule.ScheduleDayResponse
import ru.radiationx.data.system.ApiUtils
import javax.inject.Inject

class ScheduleApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val releaseParser: ReleaseParser,
    private val scheduleParser: ScheduleParser,
    private val apiConfig: ApiConfig,
    private val moshi: Moshi,
    private val apiUtils: ApiUtils
) {

    suspend fun getSchedule(): List<ScheduleDay> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "schedule",
            "filter" to "id,torrents,playlist,favorite,moon,blockedInfo",
            "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchApiResponse<List<ScheduleDayResponse>>(moshi)
            .map { it.toDomain(apiUtils, apiConfig) }
    }

}