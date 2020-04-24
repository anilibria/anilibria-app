package ru.radiationx.anilibria.screen.search

import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.anilibria.screen.SuggestionsScreen
import ru.radiationx.data.entity.app.search.SearchForm
import ru.radiationx.data.repository.SearchRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val converter: CardsDataConverter,
    private val router: Router,
    private val searchController: SearchController
) : BaseCardsViewModel() {

    private var searchForm = SearchForm()

    val progressState = MutableLiveData<Boolean>()

    override val loadOnCreate: Boolean = false

    override val progressOnRefresh: Boolean = false

    override fun onColdCreate() {
        super.onColdCreate()

        searchController.applyFormEvent.lifeSubscribe {
            searchForm = it
            onRefreshClick()
        }
    }

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = searchRepository
        .searchReleases(searchForm, requestPage)
        .map { it.data.map { converter.toCard(it) } }
        .doOnSubscribe { progressState.value = requestPage == firstPage }
        .doFinally { progressState.value = false }

    fun onSearchClick() {
        router.navigateTo(SuggestionsScreen())
    }

    override fun onLibriaCardClick(card: LibriaCard) {
        if (card.type == LibriaCard.Type.RELEASE) {
            router.navigateTo(DetailsScreen(card.id))
        }
    }
}