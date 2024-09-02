package ru.radiationx.anilibria.screen.auth.credentials

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.apinext.models.Credentials
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class AuthCredentialsViewModel(
    private val authRepository: AuthRepository,
    private val guidedRouter: GuidedRouter,
) : LifecycleViewModel() {

    val progressState = MutableStateFlow(false)
    val error = MutableStateFlow("")

    fun onLoginClicked(login: String, password: String) {
        viewModelScope.launch {
            progressState.value = true
            error.value = ""
            coRunCatching {
                authRepository.signIn(Credentials(login, password))
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