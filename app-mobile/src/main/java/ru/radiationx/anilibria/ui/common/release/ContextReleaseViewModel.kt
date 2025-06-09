package ru.radiationx.anilibria.ui.common.release

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.api.auth.AuthRepository
import ru.radiationx.data.api.auth.models.AuthState
import ru.radiationx.data.api.collections.CollectionsInteractor
import ru.radiationx.data.api.collections.models.CollectionType
import ru.radiationx.data.api.favorites.FavoritesInteractor
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.app.history.HistoryRepository
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared_app.common.SystemUtils
import javax.inject.Inject

data class ContextReleaseExtra(
    val id: ReleaseId,
    val release: Release
) : QuillExtra

class ReleaseItemViewModel @Inject constructor(
    private val argExtra: ContextReleaseExtra,
    private val collectionsInteractor: CollectionsInteractor,
    private val favoritesInteractor: FavoritesInteractor,
    private val authRepository: AuthRepository,
    private val historyRepository: HistoryRepository,
    private val systemUtils: SystemUtils,
    private val shortcutHelper: ShortcutHelper,
    private val controller: ContextReleaseController,
    private val systemMessenger: SystemMessenger
) : ViewModel() {

    private val _state = MutableStateFlow(ContextReleaseState(title = argExtra.release.names.main))
    val state = _state.asStateFlow()

    init {
        combine(
            authRepository.observeAuthState(),
            favoritesInteractor.observeIds(),
            collectionsInteractor.observeIds(),
            historyRepository.observeIds()
        ) { authState, favoriteIds, collectionIds, historyIds ->
            val isInFavorite = favoriteIds.contains(argExtra.id)
            val collectionType = collectionIds.find { it.id == argExtra.id }?.type
            val unknownTypes =
                collectionIds.filter { it.type is CollectionType.Unknown }.map { it.type }.toSet()
            val collections = CollectionType.knownTypes + unknownTypes
            val isInHistory = historyIds.contains(argExtra.id)
            _state.update {
                it.copy(
                    hasAuth = authState == AuthState.AUTH,
                    isInFavorite = isInFavorite,
                    collectionType = collectionType,
                    collections = collections,
                    isInHistory = isInHistory
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onFavoriteClick() {
        controller.toggleFavorite(argExtra.id)
    }

    fun onCollectionSelected(collectionType: CollectionType?) {
        controller.toggleCollection(argExtra.id, collectionType)
    }

    fun onHistoryClick() {
        viewModelScope.launch {
            historyRepository.removeRelease(argExtra.id)
        }
    }

    fun onCopyClick() {
        systemUtils.copy(argExtra.release.link)
        systemMessenger.showMessage("Ссылка скопирована")
    }

    fun onShareClick() {
        systemUtils.share(argExtra.release.link)
    }

    fun onShortcutClick() {
        shortcutHelper.addShortcut(argExtra.release)
    }
}