package ru.radiationx.anilibria.screen.schedule

import androidx.lifecycle.MutableLiveData
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.repository.ScheduleRepository
import ru.radiationx.shared.ktx.asDayName
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val dataConverter: CardsDataConverter,
    private val router: Router
) : LifecycleViewModel() {

    val scheduleRows = MutableLiveData<List<Pair<String, List<LibriaCard>>>>()

    override fun onCreate() {
        super.onCreate()

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
            .lifeSubscribe {
                scheduleRows.value = it
            }
    }

    fun onCardClick(card: LibriaCard) {
        router.navigateTo(DetailsScreen(card.id))
    }
}