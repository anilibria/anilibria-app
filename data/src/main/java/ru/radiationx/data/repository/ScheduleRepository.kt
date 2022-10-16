package ru.radiationx.data.repository

import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.remote.api.ScheduleApi
import ru.radiationx.data.entity.app.feed.ScheduleItem
import ru.radiationx.data.entity.app.schedule.ScheduleDay
import ru.radiationx.shared.ktx.asMsk
import ru.radiationx.shared.ktx.isSameDay
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleRepository @Inject constructor(
        private val schedulers: SchedulersProvider,
        private val scheduleApi: ScheduleApi
) {

    private val dataRelay = BehaviorRelay.create<List<ScheduleDay>>()

    fun observeSchedule(): Observable<List<ScheduleDay>> = dataRelay
            .hide()
            .observeOn(schedulers.ui())

    fun loadSchedule(): Single<List<ScheduleDay>> = scheduleApi
            .getSchedule()
            .map {
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
            .map {
                it.map {
                    it.copy(items = it.items.sortedWith(
                            compareByDescending<ScheduleItem> {
                                it.completed
                            }.then(compareByDescending {
                                it.releaseItem.torrentUpdate
                            })
                    ))
                }
            }
            .doOnSuccess {
                dataRelay.accept(it)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}