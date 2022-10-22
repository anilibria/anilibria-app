package ru.radiationx.anilibria.presentation.configuring

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moxy.InjectViewState
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.data.interactors.ConfiguringInteractor
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class ConfiguringPresenter @Inject constructor(
    private val router: Router,
    private val configuringInteractor: ConfiguringInteractor
) : BasePresenter<ConfiguringView>(router) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        configuringInteractor
            .observeScreenState()
            .onEach {
                viewState.updateScreen(it)
            }
            .launchIn(presenterScope)

        configuringInteractor.initCheck()
    }

    fun continueCheck() = configuringInteractor.repeatCheck()

    fun nextCheck() = configuringInteractor.nextCheck()

    fun skipCheck() = configuringInteractor.skipCheck()

    override fun onDestroy() {
        super.onDestroy()
        configuringInteractor.finishCheck()
    }
}