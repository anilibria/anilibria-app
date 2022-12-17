package ru.radiationx.anilibria.screen.details

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.ReleaseRepository
import toothpick.InjectConstructor

@InjectConstructor
class DetailRelatedViewModel(
    private val argExtra: DetailExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val releaseRepository: ReleaseRepository,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter
) : BaseCardsViewModel() {

    private val releaseId = argExtra.id

    override val loadOnCreate: Boolean = false

    override val defaultTitle: String = "Связанные релизы"

    override fun onColdResume() {
        super.onColdResume()
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
        val release = releaseInteractor.getFull(releaseId) ?: releaseInteractor.getItem(releaseId)
        val releaseCodes = DetailsViewModel.getReleasesFromDesc(release?.description.orEmpty())
        if (releaseCodes.isEmpty()) {
            return emptyList()
        }

        val singles = releaseCodes.map {
            flow { emit(releaseRepository.getRelease(it)) }
        }
        return merge(*singles.toTypedArray()).toList()
            .let { releases ->
                releaseInteractor.updateItemsCache(releases)
                releases.map { converter.toCard(it) }
            }
    }

    override fun hasMoreCards(newCards: List<LibriaCard>, allCards: List<LibriaCard>): Boolean =
        false

    override fun onLibriaCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }
}