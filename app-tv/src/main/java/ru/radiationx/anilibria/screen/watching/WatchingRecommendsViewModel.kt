package ru.radiationx.anilibria.screen.watching

import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.data.api.releases.ReleaseInteractor
import ru.radiationx.data.api.releases.models.ReleaseGenre
import ru.radiationx.data.api.shared.filter.legacy.GenreItem
import ru.radiationx.data.api.shared.filter.legacy.SearchForm
import ru.radiationx.data.api.shared.filter.legacy.SearchRepository
import ru.radiationx.data.app.history.HistoryRepository
import javax.inject.Inject

class WatchingRecommendsViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val searchRepository: SearchRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Рекомендации"

    override fun onResume() {
        super.onResume()
        onRefreshClick()
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> = historyRepository
        .getReleases()
        .items
        .let { releases ->
            val genresMap = mutableMapOf<ReleaseGenre, Int>()
            releases.forEach { release ->
                release.genres.forEach {
                    val currentCount = genresMap[it] ?: 0
                    genresMap[it] = currentCount + 1
                }
            }
            genresMap.toList()
                .sortedByDescending { it.second }
                .take(3)
                .map { GenreItem(it.first.name, it.first.name) }
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