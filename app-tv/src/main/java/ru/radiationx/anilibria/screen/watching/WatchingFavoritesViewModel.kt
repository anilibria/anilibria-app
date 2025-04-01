package ru.radiationx.anilibria.screen.watching

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.FavoriteRepository
import javax.inject.Inject

class WatchingFavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val releaseInteractor: ReleaseInteractor,
    authRepository: AuthRepository,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter,
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Избранное"

    // При первом создании не грузим сразу, а дождёмся onResume()
    override val loadOnCreate: Boolean = false

    override val preventClearOnRefresh: Boolean = true

    init {
        // Если пользователь вошёл в аккаунт (AuthState.AUTH),
        // то перезагружаем список
        authRepository
            .observeAuthState()
            .drop(1)
            .filter { it == AuthState.AUTH }
            .distinctUntilChanged()
            .onEach { onRefreshClick() }
            .launchIn(viewModelScope)
    }

    override fun onResume() {
        super.onResume()
        // Каждый раз при возврате на экран — обновляем данные
        onRefreshClick()
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> {
        // Подтягиваем следующий "page" из избранного,
        // одновременно обновляя кеш релизов
        val response = favoriteRepository.getFavorites(requestPage)
        releaseInteractor.updateItemsCache(response.data)
        return response.data.map { release ->
            converter.toCard(release)
        }
    }

    // ВАЖНО: теперь бесконечной прокрутки не будет
    override fun hasMoreCards(
        newCards: List<LibriaCard>,
        allCards: List<LibriaCard>
    ): Boolean {
        return false
    }

    override fun onLibriaCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }
}
