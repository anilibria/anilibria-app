package ru.radiationx.anilibria.ui.common.release

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.api.collections.CollectionsInteractor
import ru.radiationx.data.api.collections.models.CollectionType
import ru.radiationx.data.api.favorites.FavoritesInteractor
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.shared.ktx.coRunCatching
import javax.inject.Inject

class ContextReleaseController @Inject constructor(
    private val favoritesInteractor: FavoritesInteractor,
    private val collectionsInteractor: CollectionsInteractor,
    private val systemMessenger: SystemMessenger
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun toggleFavorite(id: ReleaseId) {
        scope.launch {
            coRunCatching {
                favoritesInteractor.toggle(id)
            }.onSuccess {
                systemMessenger.showMessage("Избранное обновлено")
            }.onFailure {
                systemMessenger.showMessage("Ошибка при обновлении избранного")
            }
        }
    }

    fun toggleCollection(id: ReleaseId, type: CollectionType?) {
        scope.launch {
            coRunCatching {
                if (type != null) {
                    collectionsInteractor.addRelease(id, type)
                } else {
                    collectionsInteractor.deleteRelease(id)
                }
            }.onSuccess {
                systemMessenger.showMessage("Коллекции обновлены")
            }.onFailure {
                systemMessenger.showMessage("Ошибка при обновлении коллекций")
            }
        }
    }

}