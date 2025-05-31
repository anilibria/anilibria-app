package ru.radiationx.anilibria.ui.fragments.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.analytics.features.ScheduleAnalytics
import ru.radiationx.data.apinext.models.enums.PublishDay
import ru.radiationx.data.apinext.models.enums.asDayName
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.schedule.ScheduleDay
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.repository.ScheduleRepository
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import java.util.Calendar
import javax.inject.Inject

data class ScheduleExtra(
    val day: PublishDay?,
) : QuillExtra

class ScheduleViewModel @Inject constructor(
    private val argExtra: ScheduleExtra,
    private val scheduleRepository: ScheduleRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val scheduleAnalytics: ScheduleAnalytics,
    private val releaseAnalytics: ReleaseAnalytics,
    private val systemUtils: SystemUtils,
    private val shortcutHelper: ShortcutHelper
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
                    if (scheduleDay.day.calendarDay == calendarDay) {
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
        val releaseItem = findRelease(item.release.id) ?: return
        scheduleAnalytics.releaseClick(position)
        releaseAnalytics.open(AnalyticsConstants.screen_schedule, releaseItem.id.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, item = releaseItem))
    }

    fun onCopyClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.copy(releaseItem.link)
        releaseAnalytics.copyLink(AnalyticsConstants.screen_schedule, item.id.id)
    }

    fun onShareClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.share(releaseItem.link)
        releaseAnalytics.share(AnalyticsConstants.screen_schedule, item.id.id)
    }

    fun onShortcutClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        shortcutHelper.addShortcut(releaseItem)
        releaseAnalytics.shortcut(AnalyticsConstants.screen_schedule, item.id.id)
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true) }
            coRunCatching {
                scheduleRepository.loadSchedule()
            }.onFailure {
                errorHandler.handle(it)
            }
            _state.update { it.copy(refreshing = false) }
        }
    }

    private fun findRelease(id: ReleaseId): Release? {
        return currentDays
            .flatMap { it.items }
            .find { it.releaseItem.id == id }
            ?.releaseItem
    }

    private fun handleFirstData() {
        if (firstData) {
            firstData = false
            val currentDay = argExtra.day ?: PublishDay.ofCalendar(
                Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            )
            currentDays
                .indexOfFirst { it.day == currentDay }
                .let { _state.value.dayItems.getOrNull(it) }
                ?.also { _scrollEvent.set(it) }
        }
    }
}