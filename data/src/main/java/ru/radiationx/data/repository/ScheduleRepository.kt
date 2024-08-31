package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.api.ScheduleApi
import ru.radiationx.data.entity.domain.feed.ScheduleItem
import ru.radiationx.data.entity.domain.schedule.ScheduleDay
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.interactors.ReleaseUpdateMiddleware
import ru.radiationx.data.system.ApiUtils
import ru.radiationx.shared.ktx.asMsk
import ru.radiationx.shared.ktx.isSameDay
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleRepository @Inject constructor(
    private val scheduleApi: ScheduleApi,
    private val updateMiddleware: ReleaseUpdateMiddleware,
    private val apiUtils: ApiUtils,
    private val apiConfig: ApiConfig,
) {

    private val dataRelay = MutableStateFlow<List<ScheduleDay>?>(null)

    fun observeSchedule(): Flow<List<ScheduleDay>> = dataRelay.filterNotNull()

    suspend fun loadSchedule(): List<ScheduleDay> = withContext(Dispatchers.IO) {
        scheduleApi
            .getSchedule()
            .map { it.toDomain(apiUtils, apiConfig) }
            .let { scheduleDays ->
                scheduleDays.map { scheduleDay ->
                    val currentTime = System.currentTimeMillis().asMsk()
                    val calendarDay = Calendar.getInstance().also {
                        it.timeInMillis = currentTime
                    }.get(Calendar.DAY_OF_WEEK)
                    if (scheduleDay.day == calendarDay) {

                        val scheduleItems = scheduleDay.items.map {
                            val millisTime = (it.releaseItem.updatedAt.toLong() * 1000L).asMsk()

                            val scheduleDates = listOf(
                                millisTime
                            )
                            val deviceDates = listOf(
                                currentTime,
                                (currentTime - TimeUnit.DAYS.toMillis(1)),
                                (currentTime - TimeUnit.DAYS.toMillis(2))
                            )

                            val isSameDay = scheduleDates.any { scheduleDate ->
                                deviceDates.any { deviceDate ->
                                    Date(scheduleDate).isSameDay(Date(deviceDate))
                                }
                            }
                            it.copy(completed = isSameDay)
                        }
                        scheduleDay.copy(items = scheduleItems)
                    } else {
                        scheduleDay
                    }
                }
            }
            .let { scheduleDays ->
                scheduleDays.map { scheduleDay ->
                    scheduleDay.copy(
                        items = scheduleDay.items.sortedWith(
                            compareByDescending<ScheduleItem> {
                                it.completed
                            }.then(compareByDescending {
                                it.releaseItem.updatedAt
                            })
                        )
                    )
                }
            }
            .also {
                dataRelay.value = it
            }
            .also { scheduleDays ->
                val releases = scheduleDays.map { it.items }.flatten().map { it.releaseItem }
                updateMiddleware.handle(releases)
            }
    }
}