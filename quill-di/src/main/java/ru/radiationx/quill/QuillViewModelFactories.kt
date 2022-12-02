package ru.radiationx.quill

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlin.reflect.KClass

interface QuillExtra

fun <T : ViewModel> createViewModelFactory(
    clazz: KClass<T>,
    scope: QuillScope,
    extraProvider: (() -> QuillExtra)?
): ViewModelProvider.Factory = viewModelFactory {
    addInitializer(clazz) {
        scope.apply {
            if (extraProvider != null) {
                val module = QuillModule().apply {
                    instance {
                        extraProvider.invoke()
                    }
                }
                installModules(module)
            }
        }.get(clazz)
    }
}

internal fun createQuillViewModelFactory(
    tag: String,
    scope: QuillScope,
): ViewModelProvider.Factory = viewModelFactory {
    addInitializer(QuillScopeViewModel::class) {
        QuillScopeViewModel(tag, scope)
    }
}