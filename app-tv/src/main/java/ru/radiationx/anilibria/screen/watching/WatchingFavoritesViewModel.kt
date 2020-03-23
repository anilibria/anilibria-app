package ru.radiationx.anilibria.screen.watching

import io.reactivex.Single
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.data.repository.FavoriteRepository
import toothpick.InjectConstructor

@InjectConstructor
class WatchingFavoritesViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val converter: CardsDataConverter
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Избранное"

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = favoriteRepository
        .getFavorites(requestPage)
        .map { favoriteItems ->
            favoriteItems.data.map { converter.toCard(it) }
        }
}