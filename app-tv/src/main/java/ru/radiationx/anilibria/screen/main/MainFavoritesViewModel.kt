package ru.radiationx.anilibria.screen.main

import io.reactivex.Single
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.FavoriteRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class MainFavoritesViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository,
    private val converter: CardsDataConverter,
    private val router: Router
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Обновления в избранном"

    override val loadOnCreate: Boolean = false

    override fun onCreate() {
        super.onCreate()
        if (authRepository.getAuthState() == AuthState.AUTH) {
            onRefreshClick()
        }
    }

    override fun onColdCreate() {
        super.onColdCreate()
        authRepository
            .observeUser()
            .map { it.authState }
            .distinctUntilChanged()
            .skip(1)
            .lifeSubscribe {
                if (it == AuthState.AUTH) {
                    onRefreshClick()
                }
            }
    }

    override fun getLoader(requestPage: Int): Single<List<LibriaCard>> = favoriteRepository
        .getFavorites(requestPage)
        .doOnSuccess { releaseInteractor.updateItemsCache(it.data) }
        .map { favoriteItems ->
            favoriteItems.data.sortedByDescending { it.torrentUpdate }.map { converter.toCard(it) }
        }

    override fun onLibriaCardClick(card: LibriaCard) {
        router.navigateTo(DetailsScreen(card.id))
    }
}