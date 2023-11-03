package ru.radiationx.data.downloader

interface RemoteFileHolder {

    fun generateId(): RemoteFileId

    suspend fun get(url: String): RemoteFile?

    suspend fun put(data: RemoteFileSaveData): RemoteFile
}