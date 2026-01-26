package ru.radiationx.data.app.updater.models

import ru.radiationx.data.common.Url

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by radiationx on 28.01.18.
 */

data class UpdateData(
    val hasUpdate: Boolean,
    val code: Int,
    val build: Int,
    val name: String?,
    val date: String?,
    val links: List<UpdateLink>,
    val important: List<String>,
    val added: List<String>,
    val fixed: List<String>,
    val changed: List<String>,
) {

    @Parcelize
    data class UpdateLink(
        val name: String,
        val url: Url.Absolute,
        val type: LinkType,
    ) : Parcelable

    enum class LinkType {
        FILE,
        SITE
    }
}