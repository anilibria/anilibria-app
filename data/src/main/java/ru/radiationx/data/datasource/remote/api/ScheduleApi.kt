package ru.radiationx.data.datasource.remote.api

import org.json.JSONArray
import ru.radiationx.data.ApiClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchResult
import ru.radiationx.data.datasource.remote.parsers.ReleaseParser
import ru.radiationx.data.datasource.remote.parsers.ScheduleParser
import ru.radiationx.data.entity.app.schedule.ScheduleDay
import javax.inject.Inject

class ScheduleApi @Inject constructor(
    @ApiClient private val client: IClient,
    private val releaseParser: ReleaseParser,
    private val scheduleParser: ScheduleParser,
    private val apiConfig: ApiConfig
) {

    suspend fun getSchedule(): List<ScheduleDay> {
        val args: MutableMap<String, String> = mutableMapOf(
            "query" to "schedule",
            "filter" to "id,torrents,playlist,favorite,moon,blockedInfo",
            "rm" to "true"
        )
        return client.post(apiConfig.apiUrl, args)
            .fetchResult<JSONArray>()
            .let { scheduleParser.schedule(it, releaseParser) }
    }

}