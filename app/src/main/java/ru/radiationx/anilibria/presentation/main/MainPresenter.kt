package ru.radiationx.anilibria.presentation.main

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import io.reactivex.disposables.CompositeDisposable
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.data.BlazingFastException
import ru.radiationx.anilibria.model.data.GoogleCaptchaException
import ru.radiationx.anilibria.model.data.holders.AppThemeHolder
import ru.radiationx.anilibria.model.interactors.AntiDdosInteractor
import ru.radiationx.anilibria.model.repository.AuthRepository
import ru.radiationx.anilibria.model.repository.CheckerRepository
import ru.radiationx.anilibria.model.repository.ReleaseRepository
import ru.radiationx.anilibria.presentation.IErrorHandler
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 17.12.17.
 */
@InjectViewState
class MainPresenter(
        private val router: Router,
        private val errorHandler: IErrorHandler,
        private val authRepository: AuthRepository,
        private val checkerRepository: CheckerRepository,
        private val antiDdosInteractor: AntiDdosInteractor,
        private val appThemeHolder: AppThemeHolder
) : BasePresenter<MainView>(router) {

    private var antiDdosCompositeDisposable = CompositeDisposable()

    var defaultScreen = Screens.RELEASES_SEARCH

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
        Log.e("S_DEF_LOG", "main onFirstViewAttach ${authRepository.getAuthState()} : ${antiDdosInteractor.isHardChecked}")
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
                    router.showSystemMessage("new complete: $it")
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
            router.navigateTo(Screens.AUTH)
        }

        selectTab(defaultScreen)
        authRepository
                .observeUser()
                .subscribe {
                    viewState.updateTabs()
                }
                .addToDisposable()
        viewState.onMainLogicCompleted()
    }

    fun skipAntiDdos() {
        initMain()
    }

    fun getAuthState() = authRepository.getAuthState()

    fun selectTab(screenKey: String) {
        Log.e("S_DEF_LOG", "presenter selectTab " + screenKey)
        viewState.highlightTab(screenKey)
    }

}
