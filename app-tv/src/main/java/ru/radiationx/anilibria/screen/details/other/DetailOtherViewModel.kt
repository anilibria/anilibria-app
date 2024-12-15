package ru.radiationx.anilibria.screen.details.other

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.details.DetailExtra
import ru.radiationx.data.interactors.ReleaseInteractor
import javax.inject.Inject

class DetailOtherViewModel @Inject constructor(
    private val argExtra: DetailExtra,
    private val releaseInteractor: ReleaseInteractor,
    private val guidedRouter: GuidedRouter,
) : LifecycleViewModel() {


    fun onClearClick() {
        viewModelScope.launch {
            releaseInteractor.resetAccessHistory(argExtra.id)
            guidedRouter.close()
        }
    }

    fun onMarkClick() {
        viewModelScope.launch {
            releaseInteractor.markAllViewed(argExtra.id)
            guidedRouter.close()
        }
    }
}