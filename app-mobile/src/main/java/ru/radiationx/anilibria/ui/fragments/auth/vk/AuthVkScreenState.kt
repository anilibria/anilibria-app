package ru.radiationx.anilibria.ui.fragments.auth.vk

import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState

data class AuthVkScreenState(
    val pageState: WebPageViewState = WebPageViewState.Loading,
    val showClearCookies: Boolean = false,
    val data: AuthVkData? = null
)