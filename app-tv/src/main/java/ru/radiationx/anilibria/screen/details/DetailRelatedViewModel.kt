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
import ru.radiationx.data.interactors.ReleaseInteractor
import javax.inject.Inject

class DetailRelatedViewModel @Inject constructor(
    argExtra: DetailExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter,
) : BaseCardsViewModel() {

    private val releaseId = argExtra.id

    override val loadOnCreate: Boolean = false

    override val defaultTitle: String = "Связанные релизы"

    init {
        cardsData.value = listOf(loadingCard)
        releaseInteractor
            .observeFull(releaseId)
            .map { it.description.orEmpty() }
            .distinctUntilChanged()
            .onEach {
                onRefreshClick()
            }
            .launchIn(viewModelScope)
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> {
        val releases = releaseInteractor.loadWithFranchises(releaseId).filter { it.id != releaseId }
        releaseInteractor.updateItemsCache(releases)
        return releases.map { converter.toCard(it) }
    }

    override fun hasMoreCards(newCards: List<LibriaCard>, allCards: List<LibriaCard>): Boolean =
        false

    override fun onLibriaCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }
}