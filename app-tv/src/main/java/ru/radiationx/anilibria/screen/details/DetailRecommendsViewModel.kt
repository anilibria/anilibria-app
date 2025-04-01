package ru.radiationx.anilibria.screen.details

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.data.entity.domain.release.GenreItem
import ru.radiationx.data.entity.domain.release.SeasonItem
import ru.radiationx.data.entity.domain.release.YearItem
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.ReleaseRepository
import ru.radiationx.data.repository.SearchRepository
import javax.inject.Inject

/**
 * Локальные «рекомендации» для конкретного релиза:
 * - Берём данные о годе/сезоне/жанрах релиза,
 * - Находим похожие (исключая сам),
 * - Подмешиваем пару случайных тайтлов.
 */
class DetailRecommendsViewModel @Inject constructor(
    private val releaseRepository: ReleaseRepository,
    private val searchRepository: SearchRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter,
    private val extra: DetailExtra,  // <-- Вместо 'releaseId' берём весь DetailExtra
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Похожие тайтлы"

    /**
     * При «getLoader(page)» берём релиз из БД, собираем форму (год/сезон/жанры),
     * потом делаем поиск (searchReleases), исключаем сам релиз, и
     * подмешиваем 1-2 случайных из результата для разнообразия.
     */
    override suspend fun getLoader(requestPage: Int): List<LibriaCard> {
        // 1) Загружаем релиз
        val currentRelease = withContext(Dispatchers.IO) {
            releaseRepository.getRelease(extra.id) // <-- 'extra.id' – тот самый ReleaseId
        }
        // 2) Формируем searchForm
        val form = SearchForm(
            years = currentRelease.year?.let { setOf(YearItem(it, it)) }.orEmpty(),
            seasons = currentRelease.season?.let { setOf(SeasonItem(it, it)) }.orEmpty(),
            genres = currentRelease.genres.map { g -> GenreItem(g, g) }.toSet(),
            sort = SearchForm.Sort.RATING,
            onlyCompleted = false
        )

        // 3) Поиск + исключаем сам релиз
        val searchResult = withContext(Dispatchers.IO) {
            searchRepository.searchReleases(form, requestPage)
        }
        // Обновляем кэш
        releaseInteractor.updateItemsCache(searchResult.data)
        val filteredList = searchResult.data.filterNot { it.id == extra.id }

        // 4) Подмешаем случайные 1-2
        val randomPick = searchResult.data.shuffled().take(2)
        val finalList = filteredList.union(randomPick).toList()

        // Возвращаем как LibriaCard
        return finalList.map { converter.toCard(it) }
    }

    /**
     * При клике — переходим через [cardRouter].
     */
    override fun onLibriaCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }
}
