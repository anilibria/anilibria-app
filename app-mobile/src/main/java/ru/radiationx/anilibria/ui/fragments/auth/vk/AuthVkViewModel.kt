package ru.radiationx.anilibria.ui.fragments.auth.vk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.anilibria.ui.fragments.auth.social.WebAuthSoFastDetector
import ru.radiationx.data.analytics.features.AuthVkAnalytics
import ru.radiationx.data.datasource.holders.AuthHolder
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.EventFlow
import javax.inject.Inject

data class AuthVkExtra(
    val url: String,
) : QuillExtra

class AuthVkViewModel @Inject constructor(
    private val argExtra: AuthVkExtra,
    private val authHolder: AuthHolder,
    private val router: Router,
    private val authVkAnalytics: AuthVkAnalytics
) : ViewModel() {

    private val resultPattern =
        "(\\?act=widget|anilibria\\.tv\\/public\\/vk\\.php\\?code=|vk\\.com\\/widget_comments\\.php)"

    private val detector = WebAuthSoFastDetector()
    private var currentSuccessUrl: String? = null

    private val _state = MutableStateFlow(AuthVkScreenState())
    val state = _state.asStateFlow()

    private val _reloadEvent = EventFlow<Unit>()
    val reloadEvent = _reloadEvent.observe()

    init {
        resetPage()
    }

    private fun resetPage() {
        detector.loadUrl(argExtra.url)
        _state.update { it.copy(data = AuthVkData(argExtra.url, resultPattern)) }
    }

    fun onClearDataClick() {
        viewModelScope.launch {
            currentSuccessUrl = null
            detector.reset()
            detector.clearCookies()
            detector.loadUrl(argExtra.url)
            _reloadEvent.set(Unit)
            _state.update { it.copy(showClearCookies = false) }
        }
    }

    fun onContinueClick() {
        _state.update { it.copy(showClearCookies = false) }
        currentSuccessUrl?.also { successSignVk() }
    }

    fun onSuccessAuthResult(result: String) {
        if (detector.isSoFast()) {
            currentSuccessUrl = result
            _state.update { it.copy(showClearCookies = true) }
        } else {
            successSignVk()
        }
    }

    fun onPageStateChanged(pageState: WebPageViewState) {
        if (pageState is WebPageViewState.Error) {
            authVkAnalytics.error()
        }
        _state.update { it.copy(pageState = pageState) }
    }

    private fun successSignVk() {
        viewModelScope.launch {
            authVkAnalytics.success()
            authHolder.changeVkAuth(true)
            router.exit()
        }
    }
}

data class AuthVkData(
    val url: String,
    val pattern: String,
)