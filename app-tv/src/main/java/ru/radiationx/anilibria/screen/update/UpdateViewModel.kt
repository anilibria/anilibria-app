package ru.radiationx.anilibria.screen.update

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.UpdateSourceScreen
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.entity.app.updater.UpdateData
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.shared_app.common.MimeTypeUtil
import ru.radiationx.shared_app.common.download.DownloadController
import ru.radiationx.shared_app.common.download.DownloadItem
import toothpick.InjectConstructor
import java.io.FileInputStream

@InjectConstructor
class UpdateViewModel(
    private val checkerRepository: CheckerRepository,
    private val buildConfig: SharedBuildConfig,
    private val guidedRouter: GuidedRouter,
    private val downloadController: DownloadController,
    private val updateController: UpdateController,
    private val context: Context
) : LifecycleViewModel() {

    val updateData = MutableLiveData<UpdateData>()
    val loadingShowState = MutableLiveData<Boolean>()
    val progressData = MutableLiveData<Int>()
    val actionTitle = MutableLiveData<String>()

    private var downloadItem: DownloadItem? = null
    private var downloadState: DownloadController.State? = null

    override fun onCreate() {
        super.onCreate()
        loadingShowState.value = false
        progressData.value = 0
        updateState()

        checkerRepository
            .checkUpdate(buildConfig.versionCode, true)
            .lifeSubscribe({
                updateData.value = it
                downloadItem = it.links.mapNotNull { downloadController.getDownload(it.url) }.firstOrNull()
                downloadItem?.also {
                    startDownload(it.url)
                }
            }, {
                it.printStackTrace()
            })

        updateController
            .downloadAction
            .lifeSubscribe {
                startDownload(it.url)
            }
    }

    fun onActionClick() {
        downloadItem?.also {
            val data = Uri.parse(it.localUrl)
            val type = MimeTypeUtil.getType(it.url)
            val kek = ContentResolver.SCHEME_FILE.equals(data.getScheme())
            Log.e("UpdateViewModel", "onActionClick $kek, $type, $data")
            val install = Intent(Intent.ACTION_VIEW)
            install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            install.setDataAndType(data, MimeTypeUtil.getType(it.url))
            //install.data = data
            context.startActivity(install)
            return
        }
        when (downloadState) {
            DownloadController.State.PENDING,
            DownloadController.State.RUNNING,
            DownloadController.State.PAUSED,
            DownloadController.State.SUCCESSFUL -> cancelDownloadClick()
            DownloadController.State.FAILED -> {
            }
            null -> downloadClick()
        }
    }

    private fun downloadClick() {
        val data = updateData.value ?: return
        if (data.links.size > 1) {
            guidedRouter.open(UpdateSourceScreen())
        } else {
            val link = data.links.firstOrNull() ?: return
            startDownload(link.url)
        }
    }

    private fun cancelDownloadClick() {
        val url = downloadItem?.url ?: return
        downloadController.cancelDownload(url)
    }

    private fun updateState(state: DownloadController.State? = downloadState) {
        downloadState = state
        loadingShowState.value = when (state) {
            DownloadController.State.PENDING,
            DownloadController.State.RUNNING,
            DownloadController.State.PAUSED -> true
            else -> false
        }
        actionTitle.value = when (state) {
            DownloadController.State.PENDING,
            DownloadController.State.RUNNING,
            DownloadController.State.PAUSED -> "Отмена"
            else -> "Установить"
        }
    }

    private fun startDownload(url: String) {
        Log.e("UpdateViewModel", "startDownload $url")
        val downloadItem = downloadController.getDownload(url) ?: downloadController.enqueueDownload(url)
        Log.e("UpdateViewModel", "downloadItem $downloadItem")
        this.downloadItem = downloadItem

        downloadController
            .observeProgress(downloadItem.url)
            .lifeSubscribe {
                Log.e("UpdateViewModel", "observeProgress ${downloadItem.downloadId}, $it")
                progressData.value = it
            }

        downloadController
            .observeState(downloadItem.url)
            .lifeSubscribe {
                Log.e("UpdateViewModel", "observeState ${downloadItem.downloadId}, $it")
                updateState(it)
            }
    }

}