package ru.radiationx.anilibria.screen.watching

import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.data.api.releases.ReleaseInteractor
import ru.radiationx.data.app.episodeaccess.EpisodesCheckerHolder
import ru.radiationx.data.app.history.HistoryRepository
import javax.inject.Inject

class WatchingContinueViewModel @Inject constructor(
    private val releaseInteractor: ReleaseInteractor,
    private val historyRepository: HistoryRepository,
    private val episodesCheckerHolder: EpisodesCheckerHolder,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter,
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Продолжить просмотр"

    override fun onResume() {
        super.onResume()
        onRefreshClick()
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> = episodesCheckerHolder
        .getEpisodes()
        .let { episodeAccesses ->
            episodeAccesses.sortedByDescending { it.lastAccessRaw }.map { it.id.releaseId }
        }
        .let { ids ->
            if (ids.isEmpty()) {
                return@let emptyList()
            }
            historyRepository.getReleases().items.let { releases ->
                releases.filter { ids.contains(it.id) }
            }
        }
        .let { releases ->
            releases.map { release ->
                val lastEpisode =
                    releaseInteractor.getAccesses(release.id).maxByOrNull { it.lastAccessRaw }
                Pair(release, lastEpisode)
            }
        }
        .let { pairs ->
            pairs.sortedByDescending { it.second?.lastAccessRaw }.map {
                converter.toCard(it.first)
                    .copy(description = "Вы остановились на ${it.second?.id?.id} серии")
            }
        }

    override fun hasMoreCards(newCards: List<LibriaCard>, allCards: List<LibriaCard>): Boolean =
        false

    override fun onLibriaCardClick(card: LibriaCard) {
        super.onLibriaCardClick(card)
        cardRouter.navigate(card)
    }
}