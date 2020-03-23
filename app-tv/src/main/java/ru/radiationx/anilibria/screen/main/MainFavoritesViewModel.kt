package ru.radiationx.anilibria.screen.main

import io.reactivex.Single
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.data.repository.FavoriteRepository
import toothpick.InjectConstructor

@InjectConstructor
class MainFavoritesViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val converter: CardsDataConverter
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Обновления в избранном"

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = favoriteRepository
        .getFavorites(requestPage)
        .map { favoriteItems ->
            favoriteItems.data.sortedByDescending { it.torrentUpdate }.map { converter.toCard(it) }
        }
}