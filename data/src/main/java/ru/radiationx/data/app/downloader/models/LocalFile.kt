package ru.radiationx.data.app.downloader.models

import java.io.File

data class LocalFile(
    val file: File,
    val name: String,
    val mimeType: String,
)