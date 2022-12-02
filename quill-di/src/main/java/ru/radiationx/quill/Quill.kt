package ru.radiationx.quill

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.viewModelFactory
import toothpick.Toothpick
import java.util.*
import kotlin.reflect.KClass


object Quill {

    private val scope by lazy { QuillScope(Toothpick.openRootScope()) }

    fun getRootScope(): QuillScope {
        return scope
    }

    fun closeScope(scope: QuillScope) {
        Toothpick.closeScope(scope.tpScope.name)
    }

    fun generateScopeName(): UUID {
        return UUID.randomUUID()
    }

}

fun Fragment.getParentQuillScope(): QuillScope {
    return parentFragment?.getQuillScope()
        ?: activity?.getQuillScope()
        ?: context?.getQuillScope()
        ?: Quill.getRootScope()
}

fun FragmentActivity.getParentQuillScope(): QuillScope {
    return Quill.getRootScope()
}


private fun Fragment.getQuillScopeVM(): QuillScopeViewModel {
    val factory = createQuillViewModelFactory(toString(), getParentQuillScope())
    return ViewModelProviders.of(this, factory).get()
}

private fun FragmentActivity.getQuillScopeVM(): QuillScopeViewModel {
    val factory = createQuillViewModelFactory(toString(), getParentQuillScope())
    return ViewModelProviders.of(this, factory).get()
}

fun Fragment.installQuillModules(vararg module: QuillModule) {
    getQuillScope().installModules(*module)
}

fun FragmentActivity.installQuillModules(vararg module: QuillModule) {
    getQuillScope().installModules(*module)
}

fun Fragment.getQuillScope(): QuillScope {
    return getQuillScopeVM().scope
}

fun FragmentActivity.getQuillScope(): QuillScope {
    return getQuillScopeVM().scope
}

fun Context.getQuillScope(): QuillScope {
    return Quill.getRootScope()
}

inline fun <reified T : ViewModel> Fragment.quillViewModel(
    noinline extraProvider: (() -> QuillExtra)? = null
): Lazy<T> = lazy {
    val factory = createViewModelFactory(T::class, getQuillScope(), extraProvider)
    ViewModelProviders.of(this, factory).get()
}

inline fun <reified T : ViewModel> FragmentActivity.quillViewModel(
    noinline extraProvider: (() -> QuillExtra)? = null
): Lazy<T> = lazy {
    val factory = createViewModelFactory(T::class, getQuillScope(), extraProvider)
    ViewModelProviders.of(this, factory).get()
}

inline fun <reified T : Any> Fragment.quillInject(
    qualifier: KClass<out Annotation>? = null
): Lazy<T> = lazy {
    quillGet(qualifier)
}

inline fun <reified T : Any> Fragment.quillGet(
    qualifier: KClass<out Annotation>? = null
): T {
    return getQuillScope().get(T::class, qualifier)
}

inline fun <reified T : Any> FragmentActivity.quillInject(
    qualifier: KClass<out Annotation>? = null
): Lazy<T> = lazy {
    quillGet(qualifier)
}

inline fun <reified T : Any> FragmentActivity.quillGet(
    qualifier: KClass<out Annotation>? = null
): T {
    return getQuillScope().get(T::class, qualifier)
}

inline fun <reified T : Any> Context.quillInject(
    qualifier: KClass<out Annotation>? = null
): Lazy<T> = lazy {
    quillGet(qualifier)
}

inline fun <reified T : Any> Context.quillGet(
    qualifier: KClass<out Annotation>? = null
): T {
    return Quill.getRootScope().get(T::class, qualifier)
}