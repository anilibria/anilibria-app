package ru.radiationx.quill

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.viewModelFactory
import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Binding
import toothpick.config.Module
import java.util.*
import javax.inject.Provider
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

class QuillScope(
    val tpScope: Scope
) {

    val name = tpScope.name

    fun openSubScope(): QuillScope {
        val tpSubScope = tpScope.openSubScope(Quill.generateScopeName())

        return QuillScope(tpSubScope)
    }

    fun close() {
        Quill.closeScope(this)
    }

    fun <T : Any> get(clazz: KClass<T>, qualifier: KClass<out Annotation>? = null): T {
        return qualifier
            ?.let { tpScope.getInstance(clazz.java, it.java.canonicalName) }
            ?: tpScope.getInstance(clazz.java)
    }

    fun installModules(vararg modules: QuillModule) {
        tpScope.installModules(*modules.map { it.tpModule }.toTypedArray())
    }
}

open class QuillModule {

    val tpModule = Module()

    fun <T : Any> instance(
        clazz: KClass<T>,
        qualifier: KClass<out Annotation>? = null,
        block: () -> T
    ) {
        tpModule.bind(clazz.java)
            .applyQualifier(qualifier)
            .toProviderInstance { block.invoke() }
            .providesSingleton()
    }

    fun <T : Any> single(
        clazz: KClass<T>,
        qualifier: KClass<out Annotation>? = null
    ) {
        tpModule.bind(clazz.java).applyQualifier(qualifier).singleton()
    }

    fun <P : Any, C : P> singleImpl(
        clazzParent: KClass<P>,
        clazzChild: KClass<C>,
        qualifier: KClass<out Annotation>? = null
    ) {
        tpModule.bind(clazzParent.java)
            .applyQualifier(qualifier)
            .to(clazzChild.java)
            .singleton()
    }

    fun <T : Any, P : Provider<T>> singleProvider(
        clazz: KClass<T>,
        providerClazz: KClass<out Provider<T>>,
        qualifier: KClass<out Annotation>? = null
    ) {
        tpModule.bind(clazz.java)
            .applyQualifier(qualifier)
            .toProvider(providerClazz.java)
            .providesSingleton()
    }

    inline fun <reified T : Any> single() {
        single(T::class)
    }

    inline fun <reified P : Any, reified C : P> singleImpl(
        qualifier: KClass<out Annotation>? = null
    ) {
        singleImpl(P::class, C::class, qualifier)
    }

    inline fun <reified T : Any> instance(
        qualifier: KClass<out Annotation>? = null,
        noinline block: () -> T
    ) {
        instance(T::class, qualifier, block)
    }

    inline fun <reified T : Any, reified P : Provider<T>> singleProvider(
        qualifier: KClass<out Annotation>? = null
    ) {
        singleProvider(T::class, P::class, qualifier)
    }

    private fun <T> Binding<T>.CanBeNamed.applyQualifier(
        qualifier: KClass<out Annotation>?
    ): Binding<T>.CanBeBound {
        return qualifier?.let { withName(qualifier.java) } ?: this
    }
}

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

fun Fragment.getParentQuillScope(): QuillScope {
    return parentFragment?.getQuillScope()
        ?: activity?.getQuillScope()
        ?: context?.getQuillScope()
        ?: Quill.getRootScope()
}

fun FragmentActivity.getParentQuillScope(): QuillScope {
    return Quill.getRootScope()
}

private fun createQuillViewModelFactory(
    tag: String,
    scope: QuillScope,
): ViewModelProvider.Factory = viewModelFactory {
    addInitializer(QuillScopeViewModel::class) {
        QuillScopeViewModel(tag, scope)
    }
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