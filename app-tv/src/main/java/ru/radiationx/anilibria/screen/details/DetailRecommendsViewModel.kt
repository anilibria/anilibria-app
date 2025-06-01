package ru.radiationx.anilibria.screen.details

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.data.api.releases.ReleaseInteractor
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.shared.filter.legacy.GenreItem
import ru.radiationx.data.api.shared.filter.legacy.SearchForm
import ru.radiationx.data.api.shared.filter.legacy.SearchRepository
import javax.inject.Inject

class DetailRecommendsViewModel @Inject constructor(
    argExtra: DetailExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val searchRepository: SearchRepository,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter,
) : BaseCardsViewModel() {

    private val releaseId = argExtra.id

    override val loadOnCreate: Boolean = false

    override val defaultTitle: String = "Рекомендации"

    init {
        cardsData.value = listOf(loadingCard)
        releaseInteractor
            .observeFull(releaseId)
            .map { it.genres }
            .distinctUntilChanged()
            .onEach {
                onRefreshClick()
            }
            .launchIn(viewModelScope)
    }

    private suspend fun searchGenres(genresCount: Int, requestPage: Int): List<Release> {
        return searchRepository
            .searchReleases(
                SearchForm(
                    genres = getGenres(genresCount),
                    sort = SearchForm.Sort.RATING
                ), requestPage
            )
            .let { result -> result.data.filter { it.id != releaseId } }
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> =
        searchGenres(3, requestPage)
            .ifEmpty {
                searchGenres(2, requestPage)
            }
            .also {
                releaseInteractor.updateItemsCache(it)
            }
            .let { result ->
                result.map { converter.toCard(it) }
            }

    private suspend fun getGenres(count: Int): Set<GenreItem> {
        val release = releaseInteractor.getFull(releaseId) ?: return emptySet()
        return release.genres.take(count).map { GenreItem(it.name, it.name) }.toSet()
    }

    override fun onLibriaCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }
}