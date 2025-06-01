package ru.radiationx.data.api.schedule

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import ru.radiationx.data.api.schedule.models.ScheduleDay
import ru.radiationx.data.api.schedule.models.ScheduleItem
import ru.radiationx.data.app.releaseupdate.ReleaseUpdateMiddleware
import ru.radiationx.shared.ktx.asMsk
import ru.radiationx.shared.ktx.isSameDay
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleRepository @Inject constructor(
    private val scheduleApi: ScheduleApiDataSource,
    private val updateMiddleware: ReleaseUpdateMiddleware,
) {

    private val dataRelay = MutableStateFlow<List<ScheduleDay>?>(null)

    fun observeSchedule(): Flow<List<ScheduleDay>> = dataRelay.filterNotNull()

    suspend fun loadSchedule(): List<ScheduleDay> = withContext(Dispatchers.IO) {
        scheduleApi
            .getWeek()
            .let { scheduleDays ->
                scheduleDays.map { scheduleDay ->
                    val currentTime = System.currentTimeMillis().asMsk()
                    val calendarDay = Calendar.getInstance().also {
                        it.timeInMillis = currentTime
                    }.get(Calendar.DAY_OF_WEEK)
                    if (scheduleDay.day.calendarDay == calendarDay) {

                        val scheduleItems = scheduleDay.items.map {
                            val millisTime = (it.releaseItem.updatedAt.time).asMsk()

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
                                it.releaseItem.freshAt
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