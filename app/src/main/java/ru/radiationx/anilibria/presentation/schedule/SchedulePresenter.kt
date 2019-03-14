package ru.radiationx.anilibria.presentation.schedule

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.entity.app.feed.FeedScheduleItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.schedule.ScheduleDay
import ru.radiationx.anilibria.model.repository.ScheduleRepository
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.ui.common.ErrorHandler
import ru.terrakok.cicerone.Router
import java.lang.Exception
import java.util.*
import javax.inject.Inject

@InjectViewState
class SchedulePresenter @Inject constructor(
        private val scheduleRepository: ScheduleRepository,
        private val router: Router,
        private val errorHandler: ErrorHandler
) : BasePresenter<ScheduleView>(router) {

    var argDay: Int = -1

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        scheduleRepository
                .observeSchedule()
                .subscribe {
                    val items = it.map {
                        Pair(
                                getDayName(ScheduleDay.toCalendarDay(it.day)),
                                it.items.map {
                                    FeedScheduleItem(it, false)
                                }
                        )
                    }
                    viewState.showSchedules(items)

                    val currentDay = if (argDay != -1) {
                        argDay
                    } else {
                        Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                    }
                    val index = it.indexOfFirst { ScheduleDay.toCalendarDay(it.day) == currentDay }
                    if (index != -1) {
                        viewState.scrollToDay(items[index])
                    }
                }
                .addToDisposable()
    }

    fun onItemClick(releaseItem: ReleaseItem) {
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id))
    }

    fun refresh() {
        scheduleRepository
                .loadSchedule()
                .doOnSubscribe {
                    viewState.setRefreshing(true)
                }
                .doFinally {
                    viewState.setRefreshing(false)
                }
                .subscribe({}, {
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    private fun getDayName(day: Int): String {
        return when (day) {
            Calendar.MONDAY -> "Понедельник"
            Calendar.TUESDAY -> "Вторник"
            Calendar.WEDNESDAY -> "Среда"
            Calendar.THURSDAY -> "Четверг"
            Calendar.FRIDAY -> "Пятница"
            Calendar.SATURDAY -> "Суббота"
            Calendar.SUNDAY -> "Воскресенье"
            else -> throw Exception("Not found schedule day by $day")
        }
    }
}