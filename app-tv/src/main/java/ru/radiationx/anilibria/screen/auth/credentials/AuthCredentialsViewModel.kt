package ru.radiationx.anilibria.screen.auth.credentials

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.repository.AuthRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class AuthCredentialsViewModel(
    private val authRepository: AuthRepository,
    private val router: Router,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    val progressState = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    fun onLoginClicked(login: String, password: String, code: String) {
        viewModelScope.launch {
            progressState.value = true
            error.value = ""
            runCatching {
                authRepository.signIn(login, password, code)
            }.onSuccess {
                guidedRouter.finishGuidedChain()
            }.onFailure {
                it.printStackTrace()
                error.value = it.message
            }
            progressState.value = false
        }
    }
}