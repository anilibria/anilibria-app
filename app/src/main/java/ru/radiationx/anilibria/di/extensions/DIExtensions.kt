package ru.radiationx.anilibria.di.extensions

import android.app.Activity
import android.support.v4.app.Fragment
import android.util.Log
import ru.radiationx.anilibria.di.Scopes
import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Module

object DI {
    private const val DEFAULT_SCOPE = Scopes.APP

    fun <T> get(clazz: Class<T>): T = get(DEFAULT_SCOPE, clazz)
    fun <T> get(scope: String, clazz: Class<T>): T {
        Log.d("ToothDI", "get in '$scope' class '$clazz'")
        return openScope(scope).getInstance(clazz)
    }

    fun inject(target: Any) = DI.inject(target, DEFAULT_SCOPE)
    fun inject(target: Any, scope: String) {
        Log.d("ToothDI", "inject in '$scope' to '$target'")
        return Toothpick.inject(target, openScope(scope))
    }

    fun inject(target: Any, scope: String, vararg modules: Module) {
        Log.d("ToothDI", "inject in '$scope' to '$target' with modules '${modules.joinToString { it.javaClass.canonicalName?.toString().orEmpty() }}'")
        return Toothpick.inject(target, openScope(scope).apply {
            installModules(*modules)
        })
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

fun <T> Fragment.getDependency(clazz: Class<T>): T = DI.get(clazz)
fun <T> Fragment.getDependency(scope: String, clazz: Class<T>): T = DI.get(scope, clazz)

fun <T> Activity.getDependency(clazz: Class<T>): T = DI.get(clazz)
fun <T> Activity.getDependency(scope: String, clazz: Class<T>): T = DI.get(scope, clazz)

fun Fragment.injectDependencies() = DI.inject(this)
fun Fragment.injectDependencies(scope: String) = DI.inject(this, scope)
fun Fragment.injectDependencies(scope: String, vararg modules: Module) = DI.inject(this, scope, *modules)

fun Activity.injectDependencies() = DI.inject(this)
fun Activity.injectDependencies(scope: String) = DI.inject(this, scope)
fun Activity.injectDependencies(scope: String, vararg modules: Module) = DI.inject(this, scope, *modules)

fun Fragment.closeDependenciesScope(scope: String) = DI.close(scope)
fun Activity.closeDependenciesScope(scope: String) = DI.close(scope)

