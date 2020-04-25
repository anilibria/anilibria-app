package ru.radiationx.anilibria.di

import ru.radiationx.shared_app.common.download.DownloadController
import ru.radiationx.shared_app.common.download.DownloadControllerImpl
import ru.radiationx.shared_app.common.download.DownloadsDataSource
import toothpick.config.Module

class DownloadModule : Module() {

    init {
        bind(DownloadsDataSource::class.java).singleton()
        bind(DownloadControllerImpl::class.java).to(DownloadControllerImpl::class.java).singleton()
        bind(DownloadController::class.java).to(DownloadControllerImpl::class.java).singleton()
    }
}