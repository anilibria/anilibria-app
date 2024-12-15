package ru.radiationx.anilibria.screen.auth.credentials

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

class AuthCredentialsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val guidedRouter: GuidedRouter,
) : LifecycleViewModel() {

    val progressState = MutableStateFlow(false)
    val error = MutableStateFlow("")

    fun onLoginClicked(login: String, password: String, code: String) {
        viewModelScope.launch {
            progressState.value = true
            error.value = ""
            coRunCatching {
                authRepository.signIn(login, password, code)
            }.onSuccess {
                guidedRouter.finishGuidedChain()
                error.value = "null"
            }.onFailure {
                Timber.e(it)
                error.value = it.message.toString()
            }
            progressState.value = false
        }
    }
}