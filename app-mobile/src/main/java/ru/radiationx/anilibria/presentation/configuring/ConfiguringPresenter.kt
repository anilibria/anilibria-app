package ru.radiationx.anilibria.presentation.configuring

import moxy.InjectViewState
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.interactors.ConfiguringInteractor
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class ConfiguringPresenter @Inject constructor(
    private val router: Router,
    private val configuringInteractor: ConfiguringInteractor,
    private val schedulersProvider: SchedulersProvider,
    private val errorHandler: IErrorHandler
) : BasePresenter<ConfiguringView>(router) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        configuringInteractor
            .observeScreenState()
            .observeOn(schedulersProvider.ui())
            .subscribe({
                viewState.updateScreen(it)
            }, {
                errorHandler.handle(it)
            })
            .addToDisposable()

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