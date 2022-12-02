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

    fun <T> get(clazz: Class<T>, qualifierName: String? = null): T {
        return qualifierName
            ?.let { tpScope.getInstance(clazz, it) }
            ?: tpScope.getInstance(clazz)
    }

    fun installTpModules(vararg modules: Module) {
        tpScope.installModules(*modules)
    }

    fun installModules(vararg modules: QuillModule) {
        tpScope.installModules(*modules.map { it.tpModule }.toTypedArray())
    }
}

open class QuillModule {

    val tpModule = Module()

    fun <T> instance(clazz: Class<T>, value: T) {
        tpModule.bind(clazz).toInstance(value)
    }

    fun <T> instance(clazz: Class<T>, block: () -> T) {
        tpModule.bind(clazz)
            .toProviderInstance { block.invoke() }
            .providesSingleton()
    }

    fun <T> single(clazz: Class<T>) {
        tpModule.bind(clazz).singleton()
    }

    fun <P, C : P> singleImpl(clazzParent: Class<P>, clazzChild: Class<C>) {
        tpModule.bind(clazzParent).to(clazzChild).singleton()
    }

    fun <P, C : P, A : Annotation> singleImplWithName(
        clazzParent: Class<P>,
        clazzChild: Class<C>,
        annotationClazz: Class<A>
    ) {
        tpModule.bind(clazzParent).withName(annotationClazz).to(clazzChild).singleton()
    }

    fun <T, P : Provider<T>> singleProvider(
        clazz: Class<T>,
        providerClazz: Class<out Provider<T>>
    ) {
        tpModule.bind(clazz)
            .toProvider(providerClazz)
            .providesSingleton()
    }

    fun <T, P : Provider<T>, A : Annotation> singleProviderWithName(
        clazz: Class<T>,
        providerClazz: Class<out Provider<T>>,
        annotationClazz: Class<A>
    ) {
        tpModule.bind(clazz)
            .withName(annotationClazz)
            .toProvider(providerClazz)
            .providesSingleton()
    }

    inline fun <reified T> single() {
        single(T::class.java)
    }

    inline fun <reified P, reified C : P> singleImpl() {
        singleImpl(P::class.java, C::class.java)
    }

    inline fun <reified P, reified C : P, reified A : Annotation> singleImplWithName() {
        singleImplWithName(P::class.java, C::class.java, A::class.java)
    }

    inline fun <reified T> instance(value: T) {
        instance(T::class.java, value)
    }

    inline fun <reified T> instance(noinline block: () -> T) {
        instance(T::class.java, block)
    }

    inline fun <reified T, reified P : Provider<T>> singleProvider() {
        singleProvider(T::class.java, P::class.java)
    }

    inline fun <reified T, reified P : Provider<T>, reified A : Annotation> singleProviderWithName() {
        singleProviderWithName(T::class.java, P::class.java, A::class.java)
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
                val extra = extraProvider.invoke()
                val module = QuillModule().apply {
                    instance(QuillExtra::class.java, extra)
                }
                installModules(module)
            }
        }.get(clazz.java)
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

inline fun <reified T> Fragment.quillInject(qualifierName: String? = null): Lazy<T> = lazy {
    quillGet(qualifierName)
}

inline fun <reified T> Fragment.quillGet(qualifierName: String? = null): T {
    return getQuillScope().get(T::class.java, qualifierName)
}

inline fun <reified T> FragmentActivity.quillInject(qualifierName: String? = null): Lazy<T> = lazy {
    quillGet()
}

inline fun <reified T> FragmentActivity.quillGet(qualifierName: String? = null): T {
    return getQuillScope().get(T::class.java, qualifierName)
}

inline fun <reified T> Context.quillInject(qualifierName: String? = null): Lazy<T> = lazy {
    quillGet()
}

inline fun <reified T> Context.quillGet(qualifierName: String? = null): T {
    return Quill.getRootScope().get(T::class.java, qualifierName)
}