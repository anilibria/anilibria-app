package ru.radiationx.anilibria.screen.main

import io.reactivex.Single
import ru.radiationx.anilibria.LinkCard
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.ScheduleRepository
import ru.radiationx.shared.ktx.*
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor
import java.util.*

@InjectConstructor
class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val converter: CardsDataConverter,
    private val router: Router
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Ожидается сегодня"

    override val loadMoreCard: LinkCard
        get() = LinkCard("Открыть полное расписание")

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = scheduleRepository
        .loadSchedule()
        .doOnSuccess {
            val allReleases = it.map { it.items.map { it.releaseItem } }.flatten()
            releaseInteractor.updateItemsCache(allReleases)
        }
        .map { schedueDays ->
            val currentTime = System.currentTimeMillis()
            val mskTime = System.currentTimeMillis().asMsk()

            val currentDay = currentTime.getDayOfWeek()
            val mskDay = mskTime.getDayOfWeek()


            val dayTitle = if (Date(currentTime).isSameDay(Date(mskTime))) {
                "Ожидается сегодня"
            } else {
                "Ожидается ${mskDay.asDayPretext()} ${mskDay.asDayNameDeclension().toLowerCase()} (по МСК)"
            }
            rowTitle.value = dayTitle

            val items = schedueDays.firstOrNull { it.day == mskDay }?.items?.map { it.releaseItem }.orEmpty()

            items.map { converter.toCard(it) }
        }

    override fun hasMoreCards(newCards: List<LibriaCard>, allCards: List<LibriaCard>): Boolean {
        return true
    }

    override fun onLinkCardClick() {}

    override fun onLibriaCardClick(card: LibriaCard) {
        router.navigateTo(DetailsScreen(card.id))
    }
}