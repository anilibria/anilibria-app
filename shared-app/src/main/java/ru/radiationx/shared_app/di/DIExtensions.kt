package ru.radiationx.shared_app.di

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import ru.radiationx.quill.QuillExtra
import ru.radiationx.quill.getQuillViewModel

inline fun <reified T : ViewModel> Fragment.quillParentViewModel(
    noinline extraProvider: (() -> QuillExtra)? = null
): Lazy<T> = lazy {
    val parent = requireParentFragment()
    parent.getQuillViewModel(T::class, extraProvider)
}
