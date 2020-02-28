package ru.radiationx.shared_app

import android.app.Activity
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Module
import toothpick.smoothie.lifecycle.closeOnDestroy
import toothpick.smoothie.viewmodel.closeOnViewModelCleared

object DI {
    private const val DEFAULT_SCOPE = Scopes.APP

    fun <T> get(clazz: Class<T>, name: String? = null): T = get(DEFAULT_SCOPE, clazz, name)

    fun <T> get(scope: String, clazz: Class<T>, name: String? = null): T {
        Log.d("ToothDI", "get in '$scope' class '$clazz'")
        return openScope(scope).getInstance(clazz, name)
    }

    fun inject(target: Any): Scope = inject(target, DEFAULT_SCOPE)

    fun inject(target: Any, scope: String): Scope {
        Log.d("ToothDI", "inject in '$scope' to '$target'")
        return openScope(scope).apply {
            Toothpick.inject(target, this)
        }
    }

    fun inject(target: Any, scope: String, vararg modules: Module): Scope {
        Log.d(
            "ToothDI",
            "inject in '$scope' to '$target' with modules '${modules.joinToString { it.javaClass.canonicalName?.toString().orEmpty() }}'"
        )
        return openScope(scope).apply {
            installModules(*modules)
            Toothpick.inject(target, this)
        }
    }

    private fun openScope(scope: String): Scope = Toothpick.openScopes(*(toScopes(scope).also {
        Log.d("ToothDI", "toscopes '$scope' -> '${it.joinToString()}'")
    }))

    private fun toScopes(newScope: String): Array<String> = if (newScope == DEFAULT_SCOPE)
        arrayOf(DEFAULT_SCOPE)
    else
        arrayOf(DEFAULT_SCOPE, newScope)

    fun close(scope: String) = Toothpick.closeScope(scope)
}

fun <T> Fragment.getDependency(clazz: Class<T>, name: String? = null): T = DI.get(clazz, name)
fun <T> Fragment.getDependency(scope: String, clazz: Class<T>, name: String? = null): T = DI.get(scope, clazz, name)

fun <T> FragmentActivity.getDependency(clazz: Class<T>, name: String? = null): T = DI.get(clazz, name)
fun <T> FragmentActivity.getDependency(scope: String, clazz: Class<T>, name: String? = null): T = DI.get(scope, clazz, name)

fun Fragment.injectDependencies() = DI.inject(this).closeOnDestroy(this)
fun Fragment.injectDependencies(scope: String) = DI.inject(this, scope).closeOnDestroy(this)
fun Fragment.injectDependencies(scope: String, vararg modules: Module) = DI.inject(this, scope, *modules).closeOnDestroy(this)

fun FragmentActivity.injectDependencies() = DI.inject(this).closeOnDestroy(this)
fun FragmentActivity.injectDependencies(scope: String) = DI.inject(this, scope).closeOnDestroy(this)
fun FragmentActivity.injectDependencies(scope: String, vararg modules: Module) = DI.inject(this, scope, *modules).closeOnDestroy(this)

fun Fragment.closeDependenciesScope(scope: String) = DI.close(scope)
fun FragmentActivity.closeDependenciesScope(scope: String) = DI.close(scope)

