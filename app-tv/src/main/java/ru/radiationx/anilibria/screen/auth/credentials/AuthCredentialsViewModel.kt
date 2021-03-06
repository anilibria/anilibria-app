package ru.radiationx.anilibria.screen.auth.credentials

import androidx.lifecycle.MutableLiveData
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
        progressState.value = true
        error.value = ""

        authRepository
            .signIn(login, password, code)
            .doFinally { progressState.value = false }
            .lifeSubscribe({
                guidedRouter.finishGuidedChain()
            }, {
                it.printStackTrace()
                error.value = it.message
            })
    }
}