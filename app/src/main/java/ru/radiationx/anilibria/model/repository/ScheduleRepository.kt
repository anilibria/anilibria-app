package ru.radiationx.anilibria.model.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.feed.ScheduleItem
import ru.radiationx.anilibria.entity.app.schedule.ScheduleDay
import ru.radiationx.anilibria.extension.isSameDay
import ru.radiationx.anilibria.model.data.remote.api.ScheduleApi
import ru.radiationx.anilibria.model.system.SchedulersProvider
import java.util.*
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
                    val calendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                    if (it.day == calendarDay) {
                        val scheduleItems = it.items.map {
                            val millisTime = (it.releaseItem.torrentUpdate.toLong() * 1000L)
                            val updDate = Calendar.getInstance().also {
                                it.timeInMillis = millisTime
                            }
                            val isSameDay = updDate.time.isSameDay(Date())
                            it.copy(completed = calendarDay == updDate.get(Calendar.DAY_OF_WEEK) && isSameDay)
                        }
                        it.copy(items = scheduleItems)
                    } else {
                        it
                    }
                }
            }
            .map {
                it.map {
                    it.copy(items = it.items.sortedWith(compareByDescending<ScheduleItem> { it.completed }.then(compareByDescending { it.releaseItem.torrentUpdate })))
                }
            }
            .doOnSuccess {
                dataRelay.accept(it)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}