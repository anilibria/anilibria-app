package ru.radiationx.anilibria.ui.fragments.auth.vk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.anilibria.ui.fragments.auth.social.WebAuthSoFastDetector
import ru.radiationx.data.datasource.holders.AuthHolder
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.EventFlow
import com.github.terrakok.cicerone.Router
import toothpick.InjectConstructor

data class AuthVkExtra(
    val url: String,
) : QuillExtra

@InjectConstructor
class AuthVkViewModel(
    private val argExtra: AuthVkExtra,
    private val authHolder: AuthHolder,
    private val router: Router,
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
        _state.update { it.copy(pageState = pageState) }
    }

    private fun successSignVk() {
        viewModelScope.launch {
            authHolder.changeVkAuth(true)
            router.exit()
        }
    }
}

data class AuthVkData(
    val url: String,
    val pattern: String,
)