package ru.radiationx.anilibria.screen.details

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.data.entity.domain.release.GenreItem
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.SearchRepository
import toothpick.InjectConstructor

@InjectConstructor
class DetailRecommendsViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val searchRepository: SearchRepository,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter
) : BaseCardsViewModel() {

    lateinit var releaseId: ReleaseId

    override val loadOnCreate: Boolean = false

    override val defaultTitle: String = "Рекомендации"

    override fun onCreate() {
        super.onCreate()

        cardsData.value = listOf(loadingCard)

        releaseInteractor
            .observeFull(releaseId)
            .distinctUntilChanged()
            .onEach {
                onRefreshClick()
            }
            .launchIn(viewModelScope)
    }

    private suspend fun searchGenres(genresCount: Int, requestPage: Int): List<Release> =
        searchRepository
            .searchReleases(
                SearchForm(
                    genres = getGenres(genresCount),
                    sort = SearchForm.Sort.RATING
                ), requestPage
            )
            .let { result -> result.data.filter { it.id != releaseId } }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> =
        searchGenres(3, requestPage)
            .let {
                if (it.isEmpty()) {
                    searchGenres(2, requestPage)
                } else {
                    it
                }
            }
            .also {
                releaseInteractor.updateItemsCache(it)
            }
            .let { result ->
                result.map { converter.toCard(it) }
            }

    private fun getGenres(count: Int): List<GenreItem> {
        val release = releaseInteractor.getFull(releaseId) ?: return emptyList()
        return release.genres.take(count).map { GenreItem(it, it) }
    }

    override fun onLibriaCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }
}