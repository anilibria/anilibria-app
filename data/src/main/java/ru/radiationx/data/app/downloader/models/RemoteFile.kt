package ru.radiationx.data.app.downloader.models

import ru.radiationx.data.common.ReleaseId

data class RemoteFile(
    val id: RemoteFileId,
    val url: String,
    val bucket: Bucket,
    val name: String,
    val mimeType: String,
) {
    sealed class Bucket {
        data object AppUpdates : Bucket()
        data class Torrent(val id: ReleaseId) : Bucket()
    }
}
