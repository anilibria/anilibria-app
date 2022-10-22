package ru.radiationx.anilibria.screen.launcher

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.screen.*
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.repository.AuthRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class AppLauncherViewModel(
    private val apiConfig: ApiConfig,
    private val router: Router,
    private val authRepository: AuthRepository
) : LifecycleViewModel() {

    private var firstLaunch = true

    val appReadyAction = MutableLiveData<Unit>()

    fun openRelease(id: Int) {
        router.navigateTo(DetailsScreen(id))
    }

    fun coldLaunch() {
        initWithConfig()
        //initMain()
    }

    private fun initWithConfig() {
        apiConfig
            .observeNeedConfig()
            .distinctUntilChanged()
            .onEach {
                if (it) {
                    router.newRootScreen(ConfigScreen())
                } else {
                    if (firstLaunch) {
                        initMain()
                    }
                }
            }
            .launchIn(viewModelScope)

        if (apiConfig.needConfig) {
            router.newRootScreen(ConfigScreen())
        } else {
            initMain()
        }
    }

    private fun initMain() {
        firstLaunch = false
        router.newRootScreen(MainPagesScreen())
        //router.newRootScreen(SearchScreen())
        if (authRepository.getAuthState() == AuthState.NO_AUTH) {
            router.navigateTo(AuthGuidedScreen())
        }
        appReadyAction.value = Unit
        GlobalScope.launch {
            runCatching {
                authRepository.loadUser()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

}