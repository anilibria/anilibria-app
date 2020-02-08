package ru.radiationx.anilibria.presentation.schedule

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.extension.asDayName
import ru.radiationx.data.repository.ScheduleRepository
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.terrakok.cicerone.Router
import java.util.*
import javax.inject.Inject

@InjectViewState
class SchedulePresenter @Inject constructor(
        private val scheduleRepository: ScheduleRepository,
        private val router: Router,
        private val errorHandler: IErrorHandler
) : BasePresenter<ScheduleView>(router) {

    private var firstData = true
    var argDay: Int = -1

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        scheduleRepository
                .observeSchedule()
                .subscribe {
                    val items = it.map {
                        val calendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                        var dayName = it.day.asDayName()
                        if (it.day == calendarDay) {
                            dayName += " (сегодня)"
                        }
                        Pair(dayName, it.items)
                    }
                    viewState.showSchedules(items)

                    if (firstData) {
                        firstData = false
                        val currentDay = if (argDay != -1) {
                            argDay
                        } else {
                            Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                        }
                        val index = it.indexOfFirst { it.day == currentDay }
                        if (index != -1) {
                            viewState.scrollToDay(items[index])
                        }
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
}