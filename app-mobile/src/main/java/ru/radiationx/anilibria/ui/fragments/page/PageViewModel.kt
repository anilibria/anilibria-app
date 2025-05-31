package ru.radiationx.anilibria.ui.fragments.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.features.PageAnalytics
import ru.radiationx.data.entity.common.Url
import ru.radiationx.data.entity.domain.page.PageLibria
import ru.radiationx.data.repository.PageRepository
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.coRunCatching
import javax.inject.Inject

data class PageExtra(
    val path: Url.Relative,
) : QuillExtra

class PageViewModel @Inject constructor(
    argExtra: PageExtra,
    private val pageRepository: PageRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val pageAnalytics: PageAnalytics,
) : ViewModel() {

    private val _state = MutableStateFlow(PageScreenState())
    val state = _state.asStateFlow()

    init {
        loadPage(argExtra.path)
    }

    fun onBackPressed() {
        router.exit()
    }

    private fun loadPage(pagePath: Url.Relative) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            coRunCatching {
                pageRepository.getPage(pagePath)
            }.onSuccess { data ->
                _state.update { it.copy(data = data) }
            }.onFailure {
                pageAnalytics.error()
                errorHandler.handle(it)
            }
            _state.update { it.copy(loading = false) }
        }
    }
}

data class PageScreenState(
    val loading: Boolean = false,
    val data: PageLibria? = null,
)