package ru.radiationx.anilibria.screen.profile

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.AuthGuidedScreen
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.apinext.models.User
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    val profileData = MutableStateFlow<User?>(null)

    init {
        authRepository
            .observeUser()
            .onEach { profileData.value = it }
            .launchIn(viewModelScope)
    }

    fun onSignInClick() {
        guidedRouter.open(AuthGuidedScreen())
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun onSignOutClick() {
        GlobalScope.launch {
            coRunCatching {
                authRepository.signOut()
            }.onFailure {
                Timber.e(it)
            }
        }
    }
}