package ru.radiationx.anilibria.screen.launcher

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.screen.AuthGuidedScreen
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.MainPagesScreen
import ru.radiationx.data.api.auth.AuthRepository
import ru.radiationx.data.api.auth.models.AuthState
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

class AppLauncherViewModel @Inject constructor(
    private val router: Router,
    private val authRepository: AuthRepository
) : LifecycleViewModel() {

    val appReadyState = MutableStateFlow<Unit?>(null)

    fun openRelease(id: ReleaseId) {
        router.navigateTo(DetailsScreen(id))
    }

    fun coldLaunch() {
        initMain()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun initMain() {
        viewModelScope.launch {
            router.newRootScreen(MainPagesScreen())
            if (authRepository.getAuthState() == AuthState.NO_AUTH) {
                router.navigateTo(AuthGuidedScreen())
            }
            appReadyState.value = Unit
        }
        GlobalScope.launch {
            coRunCatching {
                authRepository.loadUser()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

}