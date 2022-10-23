package ru.radiationx.anilibria.presentation.checker

import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.features.UpdaterAnalytics
import ru.radiationx.data.repository.CheckerRepository
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
    private val router: Router
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

    fun onSourceDownloadClick(title: String) {
        updaterAnalytics.sourceDownload(title)
    }
}