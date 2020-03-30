package ru.radiationx.anilibria.screen.update

import android.util.Log
import androidx.lifecycle.MutableLiveData
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.UpdateSourceScreen
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.entity.app.updater.UpdateData
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.shared_app.common.download.DownloadController
import toothpick.InjectConstructor

@InjectConstructor
class UpdateViewModel(
    private val checkerRepository: CheckerRepository,
    private val buildConfig: SharedBuildConfig,
    private val guidedRouter: GuidedRouter,
    private val downloadController: DownloadController,
    private val updateController: UpdateController
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

        updateController
            .downloadAction
            .lifeSubscribe {
                startDownload(it.url)
            }
    }

    fun onUpdateClick() {
        val data = updateData.value ?: return
        if (data.links.size > 1) {
            guidedRouter.open(UpdateSourceScreen())
        } else {
            val link = data.links.firstOrNull() ?: return
            startDownload(link.url)
        }
    }

    private fun startDownload(url: String) {
        Log.e("UpdateViewModel", "startDownload $url")
        val downloadItem = downloadController.getDownload(url) ?: downloadController.enqueueDownload(url)
        Log.e("UpdateViewModel", "downloadItem $downloadItem")
        downloadController
            .observeProgress(downloadItem.url)
            .lifeSubscribe {
                Log.e("UpdateViewModel", "observeProgress ${downloadItem.downloadId}, $it")
            }

        downloadController
            .observeState(downloadItem.url)
            .lifeSubscribe {
                Log.e("UpdateViewModel", "observeState ${downloadItem.downloadId}, $it")
            }
    }

}