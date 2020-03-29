package ru.radiationx.anilibria.screen.mainpages

import androidx.lifecycle.MutableLiveData
import ru.radiationx.anilibria.AppBuildConfig
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.AuthGuidedScreen
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.repository.CheckerRepository
import toothpick.InjectConstructor

@InjectConstructor
class MainPagesViewModel(
    private val checkerRepository: CheckerRepository,
    private val buildConfig: SharedBuildConfig,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    val hasUpdatesData = MutableLiveData<Boolean>()

    override fun onCreate() {
        super.onCreate()

        checkerRepository
            .checkUpdate(buildConfig.versionCode, true)
            .lifeSubscribe({
                hasUpdatesData.value = it.code > buildConfig.versionCode
            }, {
                it.printStackTrace()
            })
    }

    fun onAppUpdateClick() {
        guidedRouter.open(AuthGuidedScreen())
    }
}