package ru.radiationx.quill

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import toothpick.Toothpick
import java.util.*
import kotlin.reflect.KClass


object Quill {

    private val scope by lazy(LazyThreadSafetyMode.NONE) {
        QuillScope(Toothpick.openRootScope())
    }

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
    return ViewModelProvider(this, factory).get()
}

private fun FragmentActivity.getQuillScopeVM(): QuillScopeViewModel {
    val factory = createQuillViewModelFactory(toString(), getParentQuillScope())
    return ViewModelProvider(this, factory).get()
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

fun <T : ViewModel> Fragment.getQuillViewModel(
    clazz: KClass<T>,
    extraProvider: (() -> QuillExtra)? = null
): T {
    val factory = createViewModelFactory(clazz, getQuillScope(), extraProvider)
    return ViewModelProvider(this, factory)[clazz.java]
}

fun <T : ViewModel> FragmentActivity.getQuillViewModel(
    clazz: KClass<T>,
    extraProvider: (() -> QuillExtra)? = null
): T {
    val factory = createViewModelFactory(clazz, getQuillScope(), extraProvider)
    return ViewModelProvider(this, factory)[clazz.java]
}

inline fun <reified T : ViewModel> Fragment.quillViewModel(
    noinline extraProvider: (() -> QuillExtra)? = null
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    getQuillViewModel(T::class, extraProvider)
}

inline fun <reified T : ViewModel> FragmentActivity.quillViewModel(
    noinline extraProvider: (() -> QuillExtra)? = null
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    getQuillViewModel(T::class, extraProvider)
}

inline fun <reified T : Any> Fragment.quillInject(
    qualifier: KClass<out Annotation>? = null
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    quillGet(qualifier)
}

inline fun <reified T : Any> Fragment.quillGet(
    qualifier: KClass<out Annotation>? = null
): T {
    return getQuillScope().get(T::class, qualifier)
}

inline fun <reified T : Any> FragmentActivity.quillInject(
    qualifier: KClass<out Annotation>? = null
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    quillGet(qualifier)
}

inline fun <reified T : Any> FragmentActivity.quillGet(
    qualifier: KClass<out Annotation>? = null
): T {
    return getQuillScope().get(T::class, qualifier)
}

inline fun <reified T : Any> Context.quillInject(
    qualifier: KClass<out Annotation>? = null
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    quillGet(qualifier)
}

inline fun <reified T : Any> Context.quillGet(
    qualifier: KClass<out Annotation>? = null
): T {
    return Quill.getRootScope().get(T::class, qualifier)
}