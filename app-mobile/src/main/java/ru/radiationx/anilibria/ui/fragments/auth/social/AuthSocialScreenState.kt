package ru.radiationx.anilibria.ui.fragments.auth.social

import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.data.apinext.models.LoginSocial

data class AuthSocialScreenState(
    val isAuthProgress: Boolean = false,
    val pageState: WebPageViewState = WebPageViewState.Loading,
    val showClearCookies: Boolean = false,
    val data: LoginSocial? = null
)