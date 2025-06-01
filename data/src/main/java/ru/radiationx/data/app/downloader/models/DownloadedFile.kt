package ru.radiationx.data.app.downloader.models

import java.io.File

data class DownloadedFile(
    val remote: RemoteFile,
    val local: File,
)