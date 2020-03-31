package ru.radiationx.shared_app.common.download

data class DownloadItem(
    val downloadId: Long,
    val url: String,
    val localUrl: String,
    val progress: Int,
    val state: DownloadController.State,
    val reason: DownloadController.Reason?
)