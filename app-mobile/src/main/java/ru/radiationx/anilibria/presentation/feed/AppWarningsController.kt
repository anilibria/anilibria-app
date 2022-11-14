package ru.radiationx.anilibria.presentation.feed

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import ru.radiationx.anilibria.ui.fragments.feed.FeedAppWarning

class AppWarningsController {

    private val closedWarnings = MutableStateFlow<Set<String>>(emptySet())

    private val _warnings = MutableStateFlow<Map<String, FeedAppWarning>>(emptyMap())

    val warnings = _warnings.combine(closedWarnings) { warnings, closed ->
        warnings.filter { !closed.contains(it.key) }
    }

    fun put(warning: FeedAppWarning) {
        _warnings.update {
            it.plus(warning.tag to warning)
        }
    }

    fun remove(tag: String) {
        _warnings.update { it.minus(tag) }
        closedWarnings.update { it.minus(tag) }
    }

    fun close(tag: String) {
        closedWarnings.update { it.plus(tag) }
    }
}