package ru.radiationx.anilibria.di

import ru.radiationx.quill.QuillModule
import ru.radiationx.shared_app.common.download.DownloadController
import ru.radiationx.shared_app.common.download.DownloadControllerImpl
import ru.radiationx.shared_app.common.download.DownloadsDataSource

class DownloadModule : QuillModule() {

    init {
        single<DownloadsDataSource>()
        singleImpl<DownloadControllerImpl, DownloadControllerImpl>()
        singleImpl<DownloadController, DownloadControllerImpl>()
    }
}