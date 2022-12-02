package ru.radiationx.quill

import android.app.Application
import android.app.Service
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

    fun <T> get(clazz: Class<T>): T {
        return tpScope.getInstance(clazz)
    }

    fun installTpModules(vararg modules: Module) {
        tpScope.installModules(*modules)
    }

    fun installModule(module: QuillModule) {
        tpScope.installModules(module.tpModule)
    }

    fun installModule(block: QuillModule.() -> Unit) {
        val module = QuillModule().apply(block)
        installModule(module)
    }
}

class QuillModule {

    val tpModule = Module()

    fun <T> single(clazz: Class<T>) {
        tpModule.bind(clazz).singleton()
    }

    fun <P, C : P> single(clazzParent: Class<P>, clazzChild: Class<C>) {
        tpModule.bind(clazzParent).to(clazzChild).singleton()
    }

    fun <T : Any> instance(value: T) {
        val clazz = value::class.java as Class<T>
        tpModule.bind(clazz).toInstance(value)
    }

    fun <T> instance(clazz: Class<T>, value: T) {
        tpModule.bind(clazz).toInstance(value)
    }

    fun <T> instance(clazz: Class<T>, block: () -> T) {
        instance(clazz, block.invoke())
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
        ?: Quill.getRootScope()
}

fun Fragment.getQuillScopeVM(): QuillScopeViewModel {
    val fragment = this
    return ViewModelProviders.of(this, viewModelFactory {
        addInitializer(QuillScopeViewModel::class) {
            val parentScope = fragment.getParentQuillScope()
            QuillScopeViewModel(fragment.toString(), parentScope)
        }
    }).get()
}

fun FragmentActivity.getQuillScopeVM(): QuillScopeViewModel {
    val activity = this
    return ViewModelProviders.of(this, viewModelFactory {
        addInitializer(QuillScopeViewModel::class) {
            QuillScopeViewModel(activity.toString(), Quill.getRootScope())
        }
    }).get()
}

fun Fragment.installQuillModule(block: QuillModule.() -> Unit) {
    getQuillScope().installModule(block)
}

fun Fragment.installTpModules(vararg module: Module) {
    getQuillScope().installTpModules(*module)
}

fun FragmentActivity.installQuillModule(block: QuillModule.() -> Unit) {
    getQuillScope().installModule(block)
}

fun FragmentActivity.installTpModules(vararg module: Module) {
    getQuillScope().installTpModules(*module)
}

fun Fragment.getQuillScope(): QuillScope {
    return getQuillScopeVM().scope
}

fun FragmentActivity.getQuillScope(): QuillScope {
    return getQuillScopeVM().scope
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
                installModule {
                    instance(extra)
                }
            }
        }.get(clazz.java)
    }
}

inline fun <reified T : ViewModel> Fragment.viewModel(
    noinline extraProvider: (() -> QuillExtra)? = null
): Lazy<T> = lazy {
    val factory = createViewModelFactory(T::class, getQuillScope(), extraProvider)
    ViewModelProviders.of(this, factory).get()
}

inline fun <reified T> Fragment.inject(qualifierName: String? = null): Lazy<T> = lazy {
    getQuillScope().get(T::class.java)
}

inline fun <reified T> Fragment.get(qualifierName: String? = null): T =
    getQuillScope().get(T::class.java)

inline fun <reified T : ViewModel> FragmentActivity.viewModel(
    noinline extraProvider: (() -> QuillExtra)? = null
): Lazy<T> = lazy {
    val factory = createViewModelFactory(T::class, getQuillScope(), extraProvider)
    ViewModelProviders.of(this, factory).get()
}

inline fun <reified T> FragmentActivity.inject(qualifierName: String? = null): Lazy<T> = lazy {
    getQuillScope().get(T::class.java)
}

inline fun <reified T> FragmentActivity.get(qualifierName: String? = null): T =
    getQuillScope().get(T::class.java)

inline fun <reified T> Application.inject(qualifierName: String? = null): Lazy<T> = lazy {
    Quill.getRootScope().get(T::class.java)
}

inline fun <reified T> Application.get(qualifierName: String? = null): T =
    Quill.getRootScope().get(T::class.java)

inline fun <reified T> Service.inject(qualifierName: String? = null): Lazy<T> = lazy {
    Quill.getRootScope().get(T::class.java)
}

inline fun <reified T> Service.get(qualifierName: String? = null): T =
    Quill.getRootScope().get(T::class.java)