package ru.radiationx.anilibria.screen.update

import androidx.lifecycle.MutableLiveData
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.UpdateSourceScreen
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.entity.app.updater.UpdateData
import ru.radiationx.data.repository.CheckerRepository
import toothpick.InjectConstructor

@InjectConstructor
class UpdateViewModel(
    private val checkerRepository: CheckerRepository,
    private val buildConfig: SharedBuildConfig,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    val updateData = MutableLiveData<UpdateData>()

    override fun onCreate() {
        super.onCreate()

        checkerRepository
            .checkUpdate(buildConfig.versionCode, true)
            .lifeSubscribe({
                updateData.value = it
            }, {
                it.printStackTrace()
            })
    }

    fun onUpdateClick() {
        guidedRouter.open(UpdateSourceScreen())
    }

}