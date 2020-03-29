package ru.radiationx.anilibria.screen.profile

import androidx.lifecycle.MutableLiveData
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.AuthGuidedScreen
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.entity.app.other.ProfileItem
import ru.radiationx.data.repository.AuthRepository
import toothpick.InjectConstructor

@InjectConstructor
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    val profileData = MutableLiveData<ProfileItem>()

    override fun onCreate() {
        super.onCreate()

        authRepository
            .observeUser()
            .lifeSubscribe {
                profileData.value = it
            }
    }

    fun onSignInClick() {
        guidedRouter.open(AuthGuidedScreen())
    }

    fun onSignOutClick() {
        authRepository
            .signOut()
            .subscribe()
    }
}