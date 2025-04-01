package ru.radiationx.anilibria.screen.suggestions

import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.FavoriteRepository
import ru.radiationx.data.repository.HistoryRepository
import ru.radiationx.data.repository.SearchRepository
import javax.inject.Inject

class SuggestionsRecommendsViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter,

    // Допустим, у нас есть FavoriteRepository и HistoryRepository
    private val favoriteRepository: FavoriteRepository,
    private val historyRepository: HistoryRepository,
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Рекомендации"

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> {
        // 1) Получаем какие-то «интересы» пользователя.
        //    Например, ID релизов, которые добавлены в избранное (только первые 50).
        val userFavIds = favoriteRepository
            .getFavorites(page = 1)
            .data
//            .take(50)
            .map { it.id }

        // Или (по желанию) берём из истории просмотров.
        val userHistory = historyRepository.getReleases(100).items

        // 2) Для «основы» рекомендаций возьмём “топ по рейтингу”.
        //    Для простоты — та же логика, что и раньше:
        val topRated = searchRepository
            .searchReleases(SearchForm(sort = SearchForm.Sort.RATING), requestPage)

        // Обновляем внутренний кэш, если вам это нужно
        releaseInteractor.updateItemsCache(topRated.data)

        val topReleases = topRated.data

        // 3) Фильтрация (простейшая):
        //    оставляем релизы, которые пересекаются с чем-то из интересов пользователя.
        //    Например, если релиз есть в userFavIds — считаем, что это «схожий» релиз.
        val userFavoriteReleases = topReleases.filter { release ->
            userFavIds.contains(release.id)
            // А при желании можно расширить, если релиз пересекается по жанрам:
            // release.genres.any { userFavoriteGenres.contains(it) }
        }

        // 4) Берём 3 случайных релиза из топа, чтобы «подмешать» разнообразие
        val randomSubset = topReleases.shuffled().take(3)

        // 5) Объединяем два списка — «релизы по интересам» + «случайные»
        //    union убирает дубли, если вдруг совпадут ID
        val finalList = userFavoriteReleases.union(randomSubset).toList()

        // Преобразуем в LibriaCard
        return finalList.map { converter.toCard(it) }
    }

    override fun onLibriaCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }
}