package ru.radiationx.anilibria.screen.watching

import io.reactivex.Single
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.data.repository.FavoriteRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class WatchingFavoritesViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val converter: CardsDataConverter,
    private val router: Router
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Избранное"

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = favoriteRepository
        .getFavorites(requestPage)
        .map { favoriteItems ->
            favoriteItems.data.map { converter.toCard(it) }
        }

    override fun onLibriaCardClick(card: LibriaCard) {
        router.navigateTo(DetailsScreen(card.id))
    }
}