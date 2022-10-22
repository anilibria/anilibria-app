package ru.radiationx.anilibria.screen.update

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
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
    val progressState = MutableLiveData<Boolean>()
    val downloadProgressShowState = MutableLiveData<Boolean>()
    val downloadProgressData = MutableLiveData<Int>()
    val downloadActionTitle = MutableLiveData<String>()

    private var currentDownload: DownloadItem? = null
    private var downloadState: DownloadController.State? = null
    private var pendingInstall = false

    private var updatesDisposable = Disposables.disposed()
    private var completedDisposable = Disposables.disposed()

    init {
        progressState.value = true
        downloadProgressShowState.value = false
        downloadProgressData.value = 0
    }

    override fun onCreate() {
        super.onCreate()
        updateState()

        checkerRepository
            .checkUpdate(buildConfig.versionCode, false)
            .doFinally {
                progressState.value = false
            }
            .lifeSubscribe({
                updateData.value = it
                it.links.mapNotNull { downloadController.getDownload(it.url) }.firstOrNull()?.also {
                    startDownload(it.url)
                }
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
        if (currentDownload != null && currentDownload?.url == downloadItem?.url) {
            return
        }

        updatesDisposable.dispose()
        updatesDisposable = downloadController
            .observeDownload(url)
            .lifeSubscribe({
                handleUpdate(it)
            }, {
                it.printStackTrace()
            })

        completedDisposable.dispose()
        completedDisposable = downloadController
            .observeCompleted(url)
            .lifeSubscribe({
                handleComplete(it)
            }, {
                it.printStackTrace()
            })

        if (downloadItem == null) {
            downloadController.startDownload(url)
        } else {
            if (downloadItem.state == DownloadController.State.SUCCESSFUL) {
                handleComplete(downloadItem)
            } else {
                handleUpdate(downloadItem)
            }
        }


    }

    private fun handleUpdate(downloadItem: DownloadItem) {
        currentDownload = downloadItem
        downloadProgressData.value = downloadItem.progress
        updateState(downloadItem.state)
    }

    private fun handleComplete(downloadItem: DownloadItem) {
        currentDownload = null
        updateState(null)
        startPendingInstall(downloadItem)
        updatesDisposable.dispose()
        completedDisposable.dispose()
    }

    private fun startPendingInstall(downloadItem: DownloadItem) {
        if (pendingInstall && downloadItem.state == DownloadController.State.SUCCESSFUL) {
            pendingInstall = false
            installAction(downloadItem)
        }
    }

    private fun updateState(state: DownloadController.State? = downloadState) {
        downloadState = state
        downloadProgressShowState.value = when (state) {
            DownloadController.State.PENDING,
            DownloadController.State.RUNNING,
            DownloadController.State.PAUSED -> true
            else -> false
        }
        downloadActionTitle.value = when (state) {
            DownloadController.State.PENDING,
            DownloadController.State.RUNNING,
            DownloadController.State.PAUSED -> "Отмена"
            else -> "Установить"
        }
    }

    private fun installAction(downloadItem: DownloadItem) {
        val data = Uri.parse(downloadItem.localUrl)
        val type = MimeTypeUtil.getType(downloadItem.url)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val install = Intent(Intent.ACTION_VIEW)
            install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            install.setDataAndType(data, type)
            context.startActivity(install)
        } else {
            val install = Intent(Intent.ACTION_VIEW)
            install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            install.setDataAndType(data, type)
            context.startActivity(install)
        }
    }
}