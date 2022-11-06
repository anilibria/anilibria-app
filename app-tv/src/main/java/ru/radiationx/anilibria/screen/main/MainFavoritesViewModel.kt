package ru.radiationx.anilibria.screen.main

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.FavoriteRepository
import toothpick.InjectConstructor

@InjectConstructor
class MainFavoritesViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter
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
            .drop(1)
            .onEach {
                if (it == AuthState.AUTH) {
                    onRefreshClick()
                }
            }
            .launchIn(viewModelScope)
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> = favoriteRepository
        .getFavorites(requestPage)
        .also { releaseInteractor.updateItemsCache(it.data) }
        .let { favoriteItems ->
            favoriteItems.data.sortedByDescending { it.torrentUpdate }.map { converter.toCard(it) }
        }

    override fun onLibriaCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }
}