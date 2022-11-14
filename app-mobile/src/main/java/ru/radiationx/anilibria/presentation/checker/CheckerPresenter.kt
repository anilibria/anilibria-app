package ru.radiationx.anilibria.presentation.checker

import android.Manifest
import android.os.Build
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.mintrocket.lib.mintpermissions.flows.MintPermissionsDialogFlow
import ru.mintrocket.lib.mintpermissions.flows.ext.isSuccess
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.features.UpdaterAnalytics
import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.data.repository.CheckerRepository
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/**
 * Created by radiationx on 28.01.18.
 */
@InjectViewState
class CheckerPresenter @Inject constructor(
    private val checkerRepository: CheckerRepository,
    private val errorHandler: IErrorHandler,
    private val updaterAnalytics: UpdaterAnalytics,
    private val sharedBuildConfig: SharedBuildConfig,
    private val router: Router,
    private val systemUtils: SystemUtils,
    private val mintPermissionsDialogFlow: MintPermissionsDialogFlow
) : BasePresenter<CheckerView>(router) {

    var forceLoad = false

    fun submitUseTime(time: Long) {
        updaterAnalytics.useTime(time)
    }

    fun checkUpdate() {
        presenterScope.launch {
            viewState.setRefreshing(true)
            runCatching {
                checkerRepository.checkUpdate(sharedBuildConfig.versionCode, forceLoad)
            }.onSuccess {
                viewState.showUpdateData(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun onDownloadClick() {
        updaterAnalytics.downloadClick()
    }

    fun onSourceDownloadClick(link: UpdateData.UpdateLink) {
        updaterAnalytics.sourceDownload(link.name)
        decideDownload(link)
    }

    private fun decideDownload(link: UpdateData.UpdateLink) {
        when (link.type) {
            "file" -> downloadFile(link)
            "site" -> systemUtils.externalLink(link.url)
            else -> systemUtils.externalLink(link.url)
        }
    }

    private fun downloadFile(link: UpdateData.UpdateLink) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            systemUtils.systemDownloader(link.url)
            return
        }
        presenterScope.launch {
            val result =
                mintPermissionsDialogFlow.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (result.isSuccess()) {
                systemUtils.systemDownloader(link.url)
            }
        }
    }
}