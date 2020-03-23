package ru.radiationx.anilibria.screen.watching

import io.reactivex.Single
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.repository.HistoryRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class HistoryViewModel(
    private val historyRepository: HistoryRepository,
    private val converter: CardsDataConverter,
    private val router: Router
) : BaseCardsViewModel() {

    override val defaultTitle: String = "История"

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = historyRepository
        .getReleases()
        .map { historyItems ->
            historyItems.map { converter.toCard(it) }
        }

    override fun onLibriaCardClick(card: LibriaCard) {
        router.navigateTo(DetailsScreen(card.id))
    }
}