package ru.radiationx.quill

import androidx.lifecycle.ViewModel

internal class QuillScopeViewModel(
    private val parentScope: QuillScope
) : ViewModel() {

    val scope: QuillScope = parentScope.openSubScope()

    override fun onCleared() {
        super.onCleared()
        scope.close()
    }
}