package ru.radiationx.data.downloader

import java.io.File

data class LocalFile(
    val file: File,
    val name: String,
    val mimeType: String,
)

fun DownloadedFile.toLocalFile() = LocalFile(local, remote.name, remote.mimeType)
