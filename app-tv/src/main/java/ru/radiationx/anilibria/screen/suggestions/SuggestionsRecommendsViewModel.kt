package ru.radiationx.anilibria.screen.suggestions

import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.data.entity.app.search.SearchForm
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.SearchRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class SuggestionsRecommendsViewModel(
    private val searchRepository: SearchRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val converter: CardsDataConverter,
    private val router: Router
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Рекомендации"

    override val loadOnCreate: Boolean = false

    override fun onColdCreate() {
        super.onColdCreate()
        onRefreshClick()
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> = searchRepository
        .searchReleases(SearchForm(sort = SearchForm.Sort.RATING), requestPage)
        .also { releaseInteractor.updateItemsCache(it.data) }
        .let { result -> result.data.map { converter.toCard(it) } }

    override fun onLibriaCardClick(card: LibriaCard) {
        router.navigateTo(DetailsScreen(card.id))
    }
}