package ru.radiationx.data.downloader

data class RemoteFileSaveData(
    val id: RemoteFileId,
    val url: String,
    val bucket: RemoteFile.Bucket,
    val contentDisposition: String?,
    val contentType: String?,
)