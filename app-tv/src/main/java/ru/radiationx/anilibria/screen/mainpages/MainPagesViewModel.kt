package ru.radiationx.anilibria.screen.mainpages

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.SearchScreen
import ru.radiationx.anilibria.screen.SuggestionsScreen
import ru.radiationx.anilibria.screen.UpdateScreen
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class MainPagesViewModel(
    private val checkerRepository: CheckerRepository,
    private val router: Router,
) : LifecycleViewModel() {

    val hasUpdatesData = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            coRunCatching {
                checkerRepository.checkUpdate(true)
            }.onSuccess {
                hasUpdatesData.value = it.hasUpdate
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    fun onAppUpdateClick() {
        router.navigateTo(UpdateScreen())
    }

    fun onCatalogClick() {
        router.navigateTo(SearchScreen())
    }

    fun onSearchClick() {
        router.navigateTo(SuggestionsScreen())
    }
}