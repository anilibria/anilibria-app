package ru.radiationx.anilibria.screen.watching

import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.data.entity.domain.release.GenreItem
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.HistoryRepository
import ru.radiationx.data.repository.SearchRepository
import toothpick.InjectConstructor

@InjectConstructor
class WatchingRecommendsViewModel(
    private val historyRepository: HistoryRepository,
    private val searchRepository: SearchRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Рекомендации"

    override val loadOnCreate: Boolean = false

    override fun onResume() {
        super.onResume()
        onRefreshClick()
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> = historyRepository
        .getReleases()
        .let { releases ->
            val genresMap = mutableMapOf<String, Int>()
            releases.forEach { release ->
                release.genres.forEach {
                    val currentCount = genresMap[it] ?: 0
                    genresMap[it] = currentCount + 1
                }
            }
            genresMap.toList()
                .sortedByDescending { it.second }
                .take(3)
                .map { GenreItem(it.first, it.first) }
                .toSet()
        }
        .let { genres ->
            val form = SearchForm(
                genres = genres,
                sort = SearchForm.Sort.RATING
            )
            searchRepository.searchReleases(form, requestPage)
        }
        .also {
            releaseInteractor.updateItemsCache(it.data)
        }
        .let { result ->
            result.data.map { converter.toCard(it) }
        }

    override fun onLibriaCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }

}