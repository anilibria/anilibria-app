package ru.radiationx.anilibria.presentation.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.features.PageAnalytics
import ru.radiationx.data.entity.domain.page.PageLibria
import ru.radiationx.data.repository.PageRepository
import ru.radiationx.quill.QuillExtra
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

data class PageExtra(
    val path: String
) : QuillExtra

@InjectConstructor
class PageViewModel(
    private val argExtra: PageExtra,
    private val pageRepository: PageRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val pageAnalytics: PageAnalytics
) : ViewModel() {

    private val _state = MutableStateFlow(PageScreenState())
    val state = _state.asStateFlow()

    init {
        loadPage(argExtra.path)
    }

    fun onBackPressed() {
        router.exit()
    }

    private fun loadPage(pagePath: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            runCatching {
                pageRepository.getPage(pagePath)
            }.onSuccess { data ->
                _state.update { it.copy(data = data) }
            }.onFailure {
                pageAnalytics.error(it)
                errorHandler.handle(it)
            }
            _state.update { it.copy(loading = false) }
        }
    }
}

data class PageScreenState(
    val loading: Boolean = false,
    val data: PageLibria? = null
)