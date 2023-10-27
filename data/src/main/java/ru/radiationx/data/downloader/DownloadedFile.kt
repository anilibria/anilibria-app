package ru.radiationx.data.downloader

import java.io.File

data class DownloadedFile(
    val remote: RemoteFile,
    val local: File,
)