package ru.radiationx.anilibria.presentation.main

import com.arellomobile.mvp.InjectViewState
import io.reactivex.disposables.CompositeDisposable
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.data.BlazingFastException
import ru.radiationx.anilibria.model.data.GoogleCaptchaException
import ru.radiationx.anilibria.model.data.holders.AppThemeHolder
import ru.radiationx.anilibria.model.data.remote.ApiError
import ru.radiationx.anilibria.model.interactors.AntiDdosInteractor
import ru.radiationx.anilibria.model.repository.AuthRepository
import ru.radiationx.anilibria.model.repository.CheckerRepository
import ru.radiationx.anilibria.model.system.messages.SystemMessenger
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 17.12.17.
 */
@InjectViewState
class MainPresenter(
        private val router: Router,
        private val systemMessenger: SystemMessenger,
        private val errorHandler: IErrorHandler,
        private val authRepository: AuthRepository,
        private val checkerRepository: CheckerRepository,
        private val antiDdosInteractor: AntiDdosInteractor,
        private val appThemeHolder: AppThemeHolder
) : BasePresenter<MainView>(router) {

    private var antiDdosCompositeDisposable = CompositeDisposable()

    var defaultScreen = Screens.MainReleases().screenKey

    init {
        appThemeHolder
                .observeTheme()
                .subscribe {
                    viewState.changeTheme(it)
                }
                .addToDisposable()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        if (antiDdosInteractor.isHardChecked) {
            initMain()
        } else {
            initAntiDdos()
        }
    }

    private fun initAntiDdos() {
        viewState.setAntiDdosVisibility(true)
        val disposable = antiDdosInteractor
                .observerCompleteEvents()
                .subscribe {
                    systemMessenger.showMessage("new complete: $it")
                    testRequest()
                }
        antiDdosCompositeDisposable.add(disposable)
        testRequest()
    }

    private fun testRequest() {
        val disposable = checkerRepository
                .checkUnderAntiDdos()
                .subscribe({
                    initMain()
                }, {
                    errorHandler.handle(it)
                    if (it !is GoogleCaptchaException && it !is BlazingFastException) {
                        initMain()
                    }
                })
        antiDdosCompositeDisposable.add(disposable)
    }

    private fun initMain() {
        antiDdosInteractor.isHardChecked = true
        antiDdosCompositeDisposable.clear()
        viewState.setAntiDdosVisibility(false)
        if (authRepository.getAuthState() == AuthState.NO_AUTH) {
            router.navigateTo(Screens.Auth())
        }

        selectTab(defaultScreen)
        authRepository
                .observeUser()
                .subscribe {
                    viewState.updateTabs()
                }
                .addToDisposable()
        viewState.onMainLogicCompleted()
        authRepository
                .loadUser()
                .subscribe({}, {})
                .addToDisposable()
    }

    fun skipAntiDdos() {
        initMain()
    }

    fun getAuthState() = authRepository.getAuthState()

    fun selectTab(screenKey: String) {
        viewState.highlightTab(screenKey)
    }

}
