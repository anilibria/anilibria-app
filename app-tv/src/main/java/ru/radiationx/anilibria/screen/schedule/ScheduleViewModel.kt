package ru.radiationx.anilibria.screen.schedule

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.repository.ScheduleRepository
import ru.radiationx.shared.ktx.asDayName
import javax.inject.Inject

class ScheduleViewModel @Inject constructor(
    scheduleRepository: ScheduleRepository,
    private val dataConverter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter,
) : LifecycleViewModel() {

    val scheduleRows = MutableStateFlow<List<Pair<String, List<LibriaCard>>>>(emptyList())

    init {
        scheduleRepository
            .observeSchedule()
            .map { days ->
                days.map { day ->
                    val title = day.day.asDayName()
                    val cards = day.items.map { item ->
                        dataConverter.toCard(item.releaseItem)
                    }
                    Pair(title, cards)
                }
            }
            .onEach {
                scheduleRows.value = it
            }
            .launchIn(viewModelScope)
    }

    fun onCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }
}