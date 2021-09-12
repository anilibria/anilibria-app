package ru.radiationx.anilibria.ui.fragments.auth.social

import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState

data class AuthSocialScreenState(
    val isAuthProgress: Boolean = false,
    val pageState: WebPageViewState? = null,
    val showClearCookies: Boolean = false
)