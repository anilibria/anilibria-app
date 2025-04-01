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

/**
 * Показывает «Связанные релизы» (franchises) для [releaseId].
 * Наследуется от [BaseCardsViewModel], чтобы мы могли:
 *  - Загружать данные,
 *  - Вызывать onRefreshClick(),
 *  - Вставлять LoadingCard / LinkCard / LibriaCard и т.д.
 */
class DetailRelatedViewModel @Inject constructor(
    argExtra: DetailExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter,
) : BaseCardsViewModel() {

    private val releaseId = argExtra.id

    /**
     * Не загружаем сразу при onColdCreate (переопределение),
     * а ждём «observeFull(releaseId)» (или manual refresh).
     */
    override val loadOnCreate: Boolean = false

    /**
     * Текст, который виден в заголовке (rowTitle) по умолчанию.
     */
    override val defaultTitle: String = "Связанные тайтлы"

    init {
        // Сразу кинем LoadingCard (чтобы не было пустого списка)
        cardsData.value = listOf(loadingCard)

        // Следим за изменением «description» конкретного релиза,
        // и когда оно меняется — делаем refresh().
        releaseInteractor
            .observeFull(releaseId)
            .map { it.description.orEmpty() }
            .distinctUntilChanged()
            .onEach {
                onRefreshClick() // по сути reload
            }
            .launchIn(viewModelScope)
    }

    /**
     * Вызывается при загрузке карточек для указанной «страницы» (requestPage).
     * Но в данном случае у нас одна страница, где показываем все franchises.
     */
    override suspend fun getLoader(requestPage: Int): List<LibriaCard> {
        // 1) Загружаем франшизы, исключая сам релиз
        val allFranchises = releaseInteractor
            .loadWithFranchises(releaseId)
            .filter { it.id != releaseId }

        // 2) Обновляем кэш
        releaseInteractor.updateItemsCache(allFranchises)

        // 3) Преобразуем в LibriaCard
        return allFranchises.map { converter.toCard(it) }
    }

    /**
     * У нас нет «пагинации» (loadMore — не нужен),
     * поэтому всегда возвращаем false.
     */
    override fun hasMoreCards(
        newCards: List<LibriaCard>,
        allCards: List<LibriaCard>
    ): Boolean = false

    /**
     * При клике по карточке → передаём в [LibriaCardRouter].
     */
    override fun onLibriaCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }
}
