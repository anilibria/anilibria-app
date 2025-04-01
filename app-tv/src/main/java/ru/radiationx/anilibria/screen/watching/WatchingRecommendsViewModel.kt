package ru.radiationx.anilibria.screen.watching

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.data.repository.FavoriteRepository
import ru.radiationx.data.repository.SearchRepository
import ru.radiationx.data.interactors.ReleaseInteractor
import javax.inject.Inject

/**
 * «Глобальные» рекомендации на экране "Я смотрю":
 * - Извлекаем "любимые жанры" из жанров избранных релизов пользователя,
 * - Подмешиваем немного случайных тайтлов для разнообразия.
 */
class WatchingRecommendsViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter,
    private val favoriteRepository: FavoriteRepository,
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Рекомендации"

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> {
        // 1) Собираем «избранные» релизы (допустим, только первую страницу)
        val userFavList = withContext(Dispatchers.IO) {
            favoriteRepository.getFavorites(page = 1).data
        }

        // 2) Извлекаем жанры из избранных релизов, делаем множество
        val userFavGenres = userFavList
            .flatMap { it.genres }  // все жанры из каждого релиза
            .toSet()                // во множество

        // 3) Берём "топ по рейтингу" (примерно, как было раньше)
        val topRated = withContext(Dispatchers.IO) {
            // Можно собрать простую форму поиска:
            //   sort = RATING
            //   толькоCompleted = false (или true, по желанию)
            // page = requestPage
            searchRepository.searchReleases(SearchForm(sort = SearchForm.Sort.RATING), requestPage)
        }
        // Обновляем кеш (если нужно)
        releaseInteractor.updateItemsCache(topRated.data)

        // 4) Фильтруем часть релизов, у которых есть пересечение жанров c userFavGenres
        val matchedByGenres = topRated.data.filter { release ->
            release.genres.any { g -> userFavGenres.contains(g) }
        }

        // 5) Подмешиваем несколько случайных тайтлов
        val randomSubset = topRated.data.shuffled().take(3)

        // 6) Объединяем две выборки (union убирает дубли, если есть)
        val finalList = matchedByGenres.union(randomSubset).toList()

        // 7) Преобразуем в LibriaCard
        return finalList.map { converter.toCard(it) }
    }

    override fun onLibriaCardClick(card: LibriaCard) {
        // Переход на детальный экран
        cardRouter.navigate(card)
    }
}
