package ru.radiationx.quill

import android.util.Log
import androidx.lifecycle.ViewModel

class QuillScopeViewModel(
    private val logTag: String,
    private val parentScope: QuillScope
) : ViewModel() {

    val scope: QuillScope = parentScope.openSubScope()

    init {
        Log.d("kekeke", "init vm scope ${scope.name} for $logTag")
    }

    override fun onCleared() {
        super.onCleared()
        scope.close()
    }
}