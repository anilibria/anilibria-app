package ru.radiationx.data.app.downloader.mapper

import ru.radiationx.data.app.downloader.models.DownloadedFile
import ru.radiationx.data.app.downloader.models.LocalFile

fun DownloadedFile.toLocalFile() = LocalFile(local, remote.name, remote.mimeType)
