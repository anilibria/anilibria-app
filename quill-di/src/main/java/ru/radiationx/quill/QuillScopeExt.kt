package ru.radiationx.quill

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get


fun Fragment.getParentScope(): QuillScope {
    return parentFragment?.getScope()
        ?: activity?.getScope()
        ?: context?.getScope()
        ?: Quill.getRootScope()
}

fun FragmentActivity.getParentScope(): QuillScope {
    return Quill.getRootScope()
}


private fun Fragment.getQuillScopeVM(): QuillScopeViewModel {
    val factory = createQuillViewModelFactory(toString(), getParentScope())
    return ViewModelProvider(this, factory).get()
}

private fun FragmentActivity.getQuillScopeVM(): QuillScopeViewModel {
    val factory = createQuillViewModelFactory(toString(), getParentScope())
    return ViewModelProvider(this, factory).get()
}

fun Fragment.installModules(vararg module: QuillModule) {
    getScope().installModules(*module)
}

fun FragmentActivity.installModules(vararg module: QuillModule) {
    getScope().installModules(*module)
}

fun Fragment.getScope(): QuillScope {
    return getQuillScopeVM().scope
}

fun FragmentActivity.getScope(): QuillScope {
    return getQuillScopeVM().scope
}

fun Context.getScope(): QuillScope {
    return Quill.getRootScope()
}
