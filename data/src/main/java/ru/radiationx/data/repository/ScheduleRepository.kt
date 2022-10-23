package ru.radiationx.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import ru.radiationx.data.datasource.remote.api.ScheduleApi
import ru.radiationx.data.entity.app.feed.ScheduleItem
import ru.radiationx.data.entity.app.schedule.ScheduleDay
import ru.radiationx.shared.ktx.asMsk
import ru.radiationx.shared.ktx.isSameDay
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleRepository @Inject constructor(
    private val scheduleApi: ScheduleApi
) {

    private val dataRelay = MutableStateFlow<List<ScheduleDay>?>(null)

    fun observeSchedule(): Flow<List<ScheduleDay>> = dataRelay.filterNotNull()

    suspend fun loadSchedule(): List<ScheduleDay> = scheduleApi
        .getSchedule()
        .let {
            it.map {
                val currentTime = System.currentTimeMillis().asMsk()
                val calendarDay = Calendar.getInstance().also {
                    it.timeInMillis = currentTime
                }.get(Calendar.DAY_OF_WEEK)
                if (it.day == calendarDay) {

                    val scheduleItems = it.items.map {
                        val millisTime = (it.releaseItem.torrentUpdate.toLong() * 1000L).asMsk()
                        val deviceTime = currentTime

                        val scheduleDates = listOf(
                            millisTime
                        )
                        val deviceDates = listOf(
                            deviceTime,
                            (deviceTime - TimeUnit.DAYS.toMillis(1)),
                            (deviceTime - TimeUnit.DAYS.toMillis(2))
                        )


                        val updDate = Calendar.getInstance().also {
                            it.timeInMillis = millisTime
                        }

                        val isSameDay = scheduleDates.any { scheduleDate ->
                            deviceDates.any { deviceDate ->
                                Date(scheduleDate).isSameDay(Date(deviceDate))
                            }
                        }
                        val isCompleted = isSameDay
                        it.copy(completed = isCompleted)
                    }
                    it.copy(items = scheduleItems)
                } else {
                    it
                }
            }
        }
        .let {
            it.map {
                it.copy(items = it.items.sortedWith(
                    compareByDescending<ScheduleItem> {
                        it.completed
                    }.then(compareByDescending {
                        it.releaseItem.torrentUpdate
                    })
                )
                )
            }
        }
        .also {
            dataRelay.value = it
        }
}