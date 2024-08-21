package ru.radiationx.anilibria.screen.main

import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.anilibria.common.LinkCard
import ru.radiationx.anilibria.screen.ScheduleScreen
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.ScheduleRepository
import ru.radiationx.shared.ktx.asDayNameDeclension
import ru.radiationx.shared.ktx.asDayPretext
import ru.radiationx.shared.ktx.asMsk
import ru.radiationx.shared.ktx.getDayOfWeek
import ru.radiationx.shared.ktx.isSameDay
import ru.radiationx.shared.ktx.lowercaseDefault
import com.github.terrakok.cicerone.Router
import toothpick.InjectConstructor
import java.util.Date

@InjectConstructor
class MainScheduleViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val converter: CardsDataConverter,
    private val router: Router,
    private val cardRouter: LibriaCardRouter,
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Ожидается сегодня"

    override val loadMoreCard: LinkCard = LinkCard("Открыть полное расписание")

    override val preventClearOnRefresh: Boolean = true

    override fun onResume() {
        super.onResume()
        onRefreshClick()
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> = scheduleRepository
        .loadSchedule()
        .also { days ->
            val allReleases = days.map { day -> day.items.map { it.releaseItem } }.flatten()
            releaseInteractor.updateItemsCache(allReleases)
        }
        .let { schedueDays ->
            val currentTime = System.currentTimeMillis()
            val mskTime = System.currentTimeMillis().asMsk()

            val mskDay = mskTime.getDayOfWeek()


            val dayTitle = if (Date(currentTime).isSameDay(Date(mskTime))) {
                "Ожидается сегодня"
            } else {
                "Ожидается ${mskDay.asDayPretext()} ${
                    mskDay.asDayNameDeclension().lowercaseDefault()
                } (по МСК)"
            }
            rowTitle.value = dayTitle

            val items = schedueDays.firstOrNull { it.day == mskDay }?.items?.map { it.releaseItem }
                .orEmpty()

            items.map { converter.toCard(it) }
        }

    override fun hasMoreCards(newCards: List<LibriaCard>, allCards: List<LibriaCard>): Boolean {
        return true
    }

    override fun onLinkCardClick() {
        router.navigateTo(ScheduleScreen())
    }

    override fun onLinkCardBind() {
        // do nothing
    }

    override fun onLibriaCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }
}