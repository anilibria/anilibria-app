package ru.radiationx.anilibria.model.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.schedule.ScheduleDay
import ru.radiationx.anilibria.model.data.holders.ReleaseUpdateHolder
import ru.radiationx.anilibria.model.data.remote.api.ReleaseApi
import ru.radiationx.anilibria.model.data.remote.api.ScheduleApi
import ru.radiationx.anilibria.model.system.SchedulersProvider
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

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
            .doOnSuccess {
                dataRelay.accept(it)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}