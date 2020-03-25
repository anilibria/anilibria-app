package ru.radiationx.anilibria.screen.auth.main

import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.AuthCodeGuidedScreen
import ru.radiationx.anilibria.screen.AuthCredentialsGuidedScreen
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.repository.AuthRepository
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val router: Router,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    fun onCodeClick() {
        guidedRouter.open(AuthCodeGuidedScreen())
    }

    fun onClassicClick() {
        guidedRouter.open(AuthCredentialsGuidedScreen())
    }

    fun onSocialClick() {

    }

    fun onSkipClick() {
        authRepository.updateUser(AuthState.AUTH_SKIPPED)
        guidedRouter.finishGuidedChain()
    }
}