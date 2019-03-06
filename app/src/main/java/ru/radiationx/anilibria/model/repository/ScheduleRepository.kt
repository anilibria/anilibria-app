package ru.radiationx.anilibria.model.repository

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.schedule.ScheduleDay
import ru.radiationx.anilibria.model.data.holders.ReleaseUpdateHolder
import ru.radiationx.anilibria.model.data.remote.api.ReleaseApi
import ru.radiationx.anilibria.model.system.SchedulersProvider
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

class ScheduleRepository @Inject constructor(
        private val schedulers: SchedulersProvider,
        private val releaseApi: ReleaseApi,
        private val releaseUpdateHolder: ReleaseUpdateHolder
) {

    fun loadSchedule(): Single<List<ScheduleDay>> {
        return releaseApi
                .getReleases(1)
                .map {
                    it.data.chunked(ceil(it.data.size / 7f).toInt())
                }
                .map {
                    it.mapIndexed { index, list ->
                        ScheduleDay("${(index % 7) + 1}", list)
                    }
                }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
    }
}