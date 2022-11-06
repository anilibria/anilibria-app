package ru.radiationx.anilibria.screen.watching

import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.data.datasource.holders.EpisodesCheckerHolder
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.HistoryRepository
import toothpick.InjectConstructor

@InjectConstructor
class WatchingContinueViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val historyRepository: HistoryRepository,
    private val episodesCheckerHolder: EpisodesCheckerHolder,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Продолжить просмотр"

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> = episodesCheckerHolder
        .getEpisodes()
        .let {
            it.sortedByDescending { it.lastAccess }.map { it.id.releaseId }
        }
        .let { ids ->
            if (ids.isEmpty()) {
                return@let emptyList()
            }
            historyRepository.getReleases().let { releases ->
                releases.filter { ids.contains(it.id) }
            }
        }
        .let { releases ->
            releases.map { release ->
                val lastEpisode =
                    releaseInteractor.getEpisodes(release.id).maxByOrNull { it.lastAccess }
                Pair(release, lastEpisode)
            }
        }
        .let {
            it.sortedByDescending { it.second?.lastAccess }.map {
                converter.toCard(it.first)
                    .copy(description = "Вы остановились на ${it.second?.id} серии")
            }
        }

    override fun hasMoreCards(newCards: List<LibriaCard>, allCards: List<LibriaCard>): Boolean =
        false

    override fun onLibriaCardClick(card: LibriaCard) {
        super.onLibriaCardClick(card)
        cardRouter.navigate(card)
    }
}