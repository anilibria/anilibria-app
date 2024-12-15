package ru.radiationx.anilibria.screen.search

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.anilibria.screen.SuggestionsScreen
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.data.repository.SearchRepository
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val converter: CardsDataConverter,
    private val router: Router,
    private val cardRouter: LibriaCardRouter,
    searchController: SearchController,
) : BaseCardsViewModel() {

    private var searchForm = SearchForm()

    val progressState = MutableStateFlow(false)

    override val loadOnCreate: Boolean = false

    override val progressOnRefresh: Boolean = false

    init {
        searchController.applyFormEvent.onEach {
            searchForm = it
            onRefreshClick()
        }.launchIn(viewModelScope)
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> {
        progressState.value = requestPage == firstPage
        val result = searchRepository
            .searchReleases(searchForm, requestPage)
            .data.map { converter.toCard(it) }
        progressState.value = false
        return result
    }

    fun onSearchClick() {
        router.navigateTo(SuggestionsScreen())
    }

    override fun onLibriaCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }
}