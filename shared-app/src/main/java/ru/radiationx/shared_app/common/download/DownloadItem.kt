package ru.radiationx.shared_app.common.download

data class DownloadItem(
    val downloadId: Long,
    val url: String,
    val localUrl: String
)