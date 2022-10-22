package ru.radiationx.anilibria.presentation.schedule

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.fragments.schedule.ScheduleDayState
import ru.radiationx.anilibria.ui.fragments.schedule.ScheduleScreenState
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.analytics.features.ScheduleAnalytics
import ru.radiationx.data.entity.app.schedule.ScheduleDay
import ru.radiationx.data.repository.ScheduleRepository
import ru.radiationx.shared.ktx.asDayName
import ru.terrakok.cicerone.Router
import java.util.*
import javax.inject.Inject

@InjectViewState
class SchedulePresenter @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val scheduleAnalytics: ScheduleAnalytics,
    private val releaseAnalytics: ReleaseAnalytics
) : BasePresenter<ScheduleView>(router) {

    private var firstData = true
    var argDay: Int = -1

    private var currentState = ScheduleScreenState()
    private val currentDays = mutableListOf<ScheduleDay>()

    private fun updateState(block: (ScheduleScreenState) -> ScheduleScreenState) {
        currentState = block.invoke(currentState)
        viewState.showState(currentState)
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        scheduleRepository
            .observeSchedule()
            .onEach { scheduleDays ->
                currentDays.clear()
                currentDays.addAll(scheduleDays)
                val dayStates = scheduleDays.map { scheduleDay ->
                    val calendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                    var dayName = scheduleDay.day.asDayName()
                    if (scheduleDay.day == calendarDay) {
                        dayName += " (сегодня)"
                    }
                    val items = scheduleDay.items.map { it.toState() }
                    ScheduleDayState(dayName, items)
                }

                updateState {
                    it.copy(dayItems = dayStates)
                }
                handleFirstData()
            }
            .launchIn(presenterScope)
    }

    private fun handleFirstData() {
        if (firstData) {
            firstData = false
            val currentDay = if (argDay != -1) {
                argDay
            } else {
                Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            }
            currentDays
                .indexOfFirst { it.day == currentDay }
                .let { currentState.dayItems.getOrNull(it) }
                ?.also { viewState.scrollToDay(it) }
        }
    }

    fun onHorizontalScroll(position: Int) {
        scheduleAnalytics.horizontalScroll(position)
    }

    fun onItemClick(item: ScheduleItemState, position: Int) {
        val releaseItem = currentDays
            .flatMap { it.items }
            .find { it.releaseItem.id == item.releaseId }
            ?.releaseItem ?: return
        scheduleAnalytics.releaseClick(position)
        releaseAnalytics.open(AnalyticsConstants.screen_schedule, releaseItem.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id))
    }

    fun refresh() {
        presenterScope.launch {
            updateState {
                it.copy(refreshing = true)
            }
            runCatching {
                scheduleRepository.loadSchedule()
            }.onFailure {
                errorHandler.handle(it)
            }
            updateState {
                it.copy(refreshing = false)
            }
        }
    }
}