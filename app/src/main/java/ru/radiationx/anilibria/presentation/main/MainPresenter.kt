package ru.radiationx.anilibria.presentation.main

import com.arellomobile.mvp.InjectViewState
import io.reactivex.Single
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.anilibria.model.data.holders.AppThemeHolder
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.repository.AuthRepository
import ru.radiationx.anilibria.model.system.LocaleHolder
import ru.radiationx.anilibria.model.system.SchedulersProvider
import ru.radiationx.anilibria.model.system.messages.SystemMessenger
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.terrakok.cicerone.Router
import java.util.concurrent.TimeUnit
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
        private val appThemeHolder: AppThemeHolder,
        private val apiConfig: ApiConfig,
        private val schedulers: SchedulersProvider,
        private val localeHolder: LocaleHolder
) : BasePresenter<MainView>(router) {

    var defaultScreen = Screens.MainFeed().screenKey!!

    private var firstLaunch = true

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        appThemeHolder
                .observeTheme()
                .subscribe { viewState.changeTheme(it) }
                .addToDisposable()

        apiConfig
                .observeNeedConfig()
                .distinctUntilChanged()
                .observeOn(schedulers.ui())
                .subscribe({
                    if (it) {
                        viewState.showConfiguring()
                    } else {
                        viewState.hideConfiguring()
                        if (firstLaunch) {
                            initMain()
                        }
                    }
                }, {
                    it.printStackTrace()
                    throw it
                })
                .addToDisposable()

        if (apiConfig.needConfig) {
            viewState.showConfiguring()
        } else {
            initMain()
        }
    }

    private fun initMain() {
        firstLaunch = false
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
