package ru.radiationx.anilibria.presentation.main

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.data.holders.AppThemeHolder
import ru.radiationx.anilibria.model.repository.AuthRepository
import ru.radiationx.anilibria.model.system.messages.SystemMessenger
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/**
 * Created by radiationx on 17.12.17.
 */
@InjectViewState
class MainPresenter @Inject constructor(
        private val router: Router,
        private val systemMessenger: SystemMessenger,
        private val errorHandler: IErrorHandler,
        private val authRepository: AuthRepository,
        private val appThemeHolder: AppThemeHolder
) : BasePresenter<MainView>(router) {

    var defaultScreen = Screens.MainReleases().screenKey!!

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        appThemeHolder
                .observeTheme()
                .subscribe { viewState.changeTheme(it) }
                .addToDisposable()

        initMain()
    }

    private fun initMain() {
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

    fun getAuthState() = authRepository.getAuthState()

    fun selectTab(screenKey: String) {
        viewState.highlightTab(screenKey)
    }

}
