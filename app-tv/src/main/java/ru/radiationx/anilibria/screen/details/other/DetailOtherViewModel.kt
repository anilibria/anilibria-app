package ru.radiationx.anilibria.screen.details.other

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.details.DetailExtra
import ru.radiationx.data.interactors.ReleaseInteractor
import toothpick.InjectConstructor

@InjectConstructor
class DetailOtherViewModel(
    private val argExtra: DetailExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter,
) : LifecycleViewModel() {


    fun onClearClick() {
        viewModelScope.launch {
            releaseInteractor.resetEpisodesHistory(argExtra.id)
            guidedRouter.close()
        }
    }

    fun onMarkClick() {
        viewModelScope.launch {
            releaseInteractor.getFull(argExtra.id)?.also { release ->
                val accesses = release.episodes.map {
                    it.access.copy(isViewed = true)
                }
                releaseInteractor.putEpisodes(accesses)
            }
            guidedRouter.close()
        }
    }
}