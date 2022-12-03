package ru.radiationx.anilibria.ui.fragments.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.analytics.features.ScheduleAnalytics
import ru.radiationx.data.entity.domain.schedule.ScheduleDay
import ru.radiationx.data.repository.ScheduleRepository
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.asDayName
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor
import java.util.*

data class ScheduleExtra(
    val day: Int?
) : QuillExtra

@InjectConstructor
class ScheduleViewModel(
    private val argExtra: ScheduleExtra,
    private val scheduleRepository: ScheduleRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val scheduleAnalytics: ScheduleAnalytics,
    private val releaseAnalytics: ReleaseAnalytics
) : ViewModel() {

    private var firstData = true

    private val _state = MutableStateFlow(ScheduleScreenState())
    val state = _state.asStateFlow()

    private val _scrollEvent = EventFlow<ScheduleDayState>()
    val scrollEvent = _scrollEvent.observe()

    private val currentDays = mutableListOf<ScheduleDay>()

    init {
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

                _state.update {
                    it.copy(dayItems = dayStates)
                }
                handleFirstData()
            }
            .launchIn(viewModelScope)
    }

    fun onBackPressed() {
        router.exit()
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
        releaseAnalytics.open(AnalyticsConstants.screen_schedule, releaseItem.id.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id))
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true) }
            runCatching {
                scheduleRepository.loadSchedule()
            }.onFailure {
                errorHandler.handle(it)
            }
            _state.update { it.copy(refreshing = false) }
        }
    }

    private fun handleFirstData() {
        if (firstData) {
            firstData = false
            val currentDay = argExtra.day ?: Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            currentDays
                .indexOfFirst { it.day == currentDay }
                .let { _state.value.dayItems.getOrNull(it) }
                ?.also { _scrollEvent.set(it) }
        }
    }
}