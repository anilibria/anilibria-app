package ru.radiationx.anilibria.presentation.auth.vk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.presentation.auth.social.WebAuthSoFastDetector
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.anilibria.ui.fragments.auth.vk.AuthVkScreenState
import ru.radiationx.data.datasource.holders.AuthHolder
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

@InjectConstructor
class AuthVkViewModel(
    private val authHolder: AuthHolder,
    private val router: Router
) : ViewModel() {

    private val resultPattern =
        "(\\?act=widget|anilibria\\.tv\\/public\\/vk\\.php\\?code=|vk\\.com\\/widget_comments\\.php)"

    var argUrl: String = ""

    private val detector = WebAuthSoFastDetector()
    private var currentSuccessUrl: String? = null

    private val _state = MutableStateFlow(AuthVkScreenState())
    val state = _state.asStateFlow()

    init {
        resetPage()
    }

    private fun resetPage() {
        detector.loadUrl(argUrl)
        _state.update { it.copy(data = AuthVkData(argUrl, resultPattern)) }
    }

    fun onClearDataClick() {
        currentSuccessUrl = null
        detector.reset()
        detector.clearCookies()
        resetPage()
        _state.update { it.copy(showClearCookies = false) }
    }

    fun onContinueClick() {
        _state.update { it.copy(showClearCookies = false) }
        currentSuccessUrl?.also { successSignVk(it) }
    }

    fun onSuccessAuthResult(result: String) {
        if (detector.isSoFast()) {
            currentSuccessUrl = result
            _state.update { it.copy(showClearCookies = true) }
        } else {
            successSignVk(result)
        }
    }

    fun onPageStateChanged(pageState: WebPageViewState) {
        _state.update { it.copy(pageState = pageState) }
    }

    private fun successSignVk(resultUrl: String) {
        viewModelScope.launch {
            authHolder.changeVkAuth(true)
            router.exit()
        }
    }
}

data class AuthVkData(
    val url: String,
    val pattern: String
)