package ru.radiationx.anilibria.screen.update.source

import androidx.lifecycle.MutableLiveData
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.update.UpdateController
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.entity.app.updater.UpdateData
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.common.download.DownloadController
import toothpick.InjectConstructor

@InjectConstructor
class UpdateSourceViewModel(
    private val checkerRepository: CheckerRepository,
    private val buildConfig: SharedBuildConfig,
    private val guidedRouter: GuidedRouter,
    private val systemUtils: SystemUtils,
    private val updateController: UpdateController
) : LifecycleViewModel() {

    val sourcesData = MutableLiveData<List<UpdateData.UpdateLink>>()

    override fun onCreate() {
        super.onCreate()

        checkerRepository
            .checkUpdate(buildConfig.versionCode, false)
            .lifeSubscribe({
                sourcesData.value = it.links
            }, {
                it.printStackTrace()
            })
    }

    fun onLinkClick(index: Int) {
        val link = sourcesData.value?.getOrNull(index) ?: return
        when (link.type) {
            "file" -> updateController.downloadAction.accept(link)
            "site" -> systemUtils.externalLink(link.url)
            else -> systemUtils.externalLink(link.url)
        }
        guidedRouter.close()
    }
}