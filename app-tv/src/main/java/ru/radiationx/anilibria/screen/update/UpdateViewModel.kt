package ru.radiationx.anilibria.screen.update

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

    private var currentDownload: DownloadItem? = null
    private var downloadState: DownloadController.State? = null
    private var pendingInstall = false


    override fun onCreate() {
        super.onCreate()
        loadingShowState.value = false
        progressData.value = 0
        updateState()

        checkerRepository
            .checkUpdate(buildConfig.versionCode, true)
            .lifeSubscribe({
                updateData.value = it
                /*it.links.mapNotNull { downloadController.getDownload(it.url) }.firstOrNull()?.also {
                    startDownload(it.url)
                }*/
            }, {
                it.printStackTrace()
            })

        updateController
            .downloadAction
            .lifeSubscribe {
                pendingInstall = true
                startDownload(it.url)
            }
    }

    fun onActionClick() {
        when (downloadState) {
            DownloadController.State.PENDING,
            DownloadController.State.RUNNING,
            DownloadController.State.PAUSED,
            DownloadController.State.FAILED -> cancelDownloadClick()
            else -> downloadClick()
        }
    }

    private fun downloadClick() {
        val data = updateData.value ?: return
        if (data.links.size > 1) {
            guidedRouter.open(UpdateSourceScreen())
        } else {
            val link = data.links.firstOrNull() ?: return
            updateController.downloadAction.accept(link)
        }
    }

    private fun cancelDownloadClick() {
        val url = currentDownload?.url ?: return
        downloadController.removeDownload(url)
    }

    private fun startDownload(url: String) {
        val downloadItem = downloadController.getDownload(url)
        currentDownload = downloadItem
        Log.e("UpdateViewModel", "startDownload by url $url")
        Log.e("UpdateViewModel", "startDownload item $downloadItem")
        if (downloadItem == null) {
            downloadController.startDownload(url)
        } else {
            updateState(downloadItem.state)
        }

        downloadController
            .observeDownload(url)
            .lifeSubscribe {
                currentDownload = it
                Log.e("UpdateViewModel", "observeDownload ${currentDownload?.downloadId}, $it")
                progressData.value = it.progress
                updateState(it.state)
            }

        downloadController
            .observeCompleted(url)
            .lifeSubscribe {
                currentDownload = null
                pendingInstall = false
                updateState(null)
            }

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

        if (pendingInstall && state == DownloadController.State.SUCCESSFUL) {
            pendingInstall = false
            currentDownload?.also {
                installAction(it)
            }
        }
    }

    private fun installAction(downloadItem: DownloadItem) {
        val data = Uri.parse(downloadItem.localUrl)
        val type = MimeTypeUtil.getType(downloadItem.url)
        val kek = ContentResolver.SCHEME_FILE.equals(data.getScheme())
        Log.e("UpdateViewModel", "onActionClick $kek, $type, $data")
        val install = Intent(Intent.ACTION_VIEW)
        install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        install.setDataAndType(data, type)
        //install.data = data
        context.startActivity(install)
    }


}