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
import ru.radiationx.anilibria.common.franchiseOrder
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
            .map { it.franchises }
            .distinctUntilChanged()
            .onEach {
                onRefreshClick()
            }
            .launchIn(viewModelScope)
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> {
        val root = requireNotNull(releaseInteractor.getFull(releaseId))
        val releases = releaseInteractor.loadWithFranchises(releaseId)
        releaseInteractor.updateItemsCache(releases)
        val byId = releases.associateBy { it.id }
        val franchiseName = root.franchises.singleOrNull()?.info?.name
        rowTitle.value = listOfNotNull(defaultTitle, "Порядок просмотра", franchiseName)
            .joinToString(" • ")
        return franchiseOrder(root.franchises).mapNotNull { entry ->
            byId[entry.release.id]?.let { release ->
                val current = release.id == releaseId
                converter.toCard(release).let { card ->
                    card.copy(
                        description = listOfNotNull(
                            "№${entry.ordinal} в порядке просмотра",
                            "Открыт сейчас".takeIf { current },
                            card.description,
                        ).joinToString(" • "),
                        franchiseOrdinal = entry.ordinal,
                        isCurrentRelease = current,
                    )
                }
            }
        }
    }

    override fun hasMoreCards(newCards: List<LibriaCard>, allCards: List<LibriaCard>): Boolean =
        false

    override fun onLibriaCardClick(card: LibriaCard) {
        if (!card.isCurrentRelease) cardRouter.navigate(card)
    }
}
