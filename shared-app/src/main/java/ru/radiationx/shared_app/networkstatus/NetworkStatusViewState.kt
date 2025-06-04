package ru.radiationx.shared_app.networkstatus

import androidx.annotation.ColorRes

data class NetworkStatusViewState(
    val text: String,
    @ColorRes val colorRes: Int,
    val isVisible: Boolean
)