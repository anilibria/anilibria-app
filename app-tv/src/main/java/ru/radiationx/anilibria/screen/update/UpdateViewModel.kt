package ru.radiationx.anilibria.screen.update

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.mintrocket.lib.mintpermissions.flows.MintPermissionsDialogFlow
import ru.mintrocket.lib.mintpermissions.flows.MintPermissionsFlow
import ru.mintrocket.lib.mintpermissions.flows.ext.isSuccess
import ru.radiationx.anilibria.common.LeanbackDialogContentConsumer
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.UpdateSourceScreen
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.MimeTypeUtil
import ru.radiationx.shared_app.common.download.DownloadController
import ru.radiationx.shared_app.common.download.DownloadItem
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class UpdateViewModel(
    private val checkerRepository: CheckerRepository,
    private val buildConfig: SharedBuildConfig,
    private val guidedRouter: GuidedRouter,
    private val downloadController: DownloadController,
    private val updateController: UpdateController,
    private val mintPermissionsDialogFlow: MintPermissionsDialogFlow,
    private val context: Context,
) : LifecycleViewModel() {

    val updateData = MutableStateFlow<UpdateData?>(null)
    val progressState = MutableStateFlow(false)
    val downloadProgressShowState = MutableStateFlow(false)
    val downloadProgressData = MutableStateFlow(0)
    val downloadActionTitle = MutableStateFlow<String?>(null)

    private var currentDownload: DownloadItem? = null
    private var downloadState: DownloadController.State? = null
    private var pendingInstall = false

    private var updatesJob: Job? = null
    private var completedJob: Job? = null

    init {
        progressState.value = true
        downloadProgressShowState.value = false
        downloadProgressData.value = 0
        updateState()

        viewModelScope.launch {
            progressState.value = true
            coRunCatching {
                checkerRepository
                    .checkUpdate(buildConfig.versionCode, false)
            }.onSuccess { update ->
                updateData.value = update
                update.links
                    .firstNotNullOfOrNull { downloadController.getDownload(it.url) }
                    ?.also {
                        startDownload(it.url)
                    }
            }.onFailure {
                Timber.e(it)
            }
            progressState.value = false
        }
        updateController
            .downloadAction
            .onEach {
                pendingInstall = true
                startDownload(it.url)
            }
            .launchIn(viewModelScope)
    }

    fun onActionClick() {
        when (downloadState) {
            DownloadController.State.PENDING,
            DownloadController.State.RUNNING,
            DownloadController.State.PAUSED,
            DownloadController.State.FAILED,
            -> cancelDownloadClick()

            else -> downloadClick()
        }
    }

    private fun downloadClick() {
        val data = updateData.value ?: return
        if (data.links.size > 1) {
            guidedRouter.open(UpdateSourceScreen())
        } else {
            val link = data.links.firstOrNull() ?: return
            updateController.downloadAction.emit(link)
        }
    }

    private fun cancelDownloadClick() {
        val url = currentDownload?.url ?: return
        downloadController.removeDownload(url)
    }

    private fun startDownload(url: String) {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val result =
                    mintPermissionsDialogFlow.request(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        MintPermissionsFlow.defaultDialogConfig.copy(
                            contentConsumer = LeanbackDialogContentConsumer()
                        )
                    )
                if (!result.isSuccess()) {
                    return@launch
                }
            }


            val downloadItem = downloadController.getDownload(url)
            if (currentDownload != null && currentDownload?.url == downloadItem?.url) {
                return@launch
            }

            updatesJob?.cancel()
            updatesJob = downloadController
                .observeDownload(url)
                .onEach {
                    handleUpdate(it)
                }
                .launchIn(viewModelScope)

            completedJob?.cancel()
            completedJob = downloadController
                .observeCompleted(url)
                .onEach {
                    handleComplete(it)
                }
                .launchIn(viewModelScope)

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
        updatesJob?.cancel()
        completedJob?.cancel()
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
            DownloadController.State.PAUSED,
            -> true

            else -> false
        }
        downloadActionTitle.value = when (state) {
            DownloadController.State.PENDING,
            DownloadController.State.RUNNING,
            DownloadController.State.PAUSED,
            -> "Отмена"

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