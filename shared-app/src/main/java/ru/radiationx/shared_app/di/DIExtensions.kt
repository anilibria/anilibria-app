package ru.radiationx.shared_app.di

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Module

object DI {
    const val DEFAULT_SCOPE = "app_scope"

    fun <T> get(clazz: Class<T>, vararg scopes: String): T = openScopes(*scopes).getInstance(clazz)

    fun inject(target: Any, vararg scopes: String): Scope = openScopes(*scopes).also { scope ->
        Toothpick.inject(target, scope)
    }

    fun inject(target: Any, modules: Array<out Module>, vararg scopes: String): Scope = openScopes(
        *scopes
    )
        .withModules(*modules)
        .also { scope ->
            Toothpick.inject(target, scope)
        }

    fun Scope.withModules(vararg modules: Module) = apply { installModules(*modules) }

    // Open DEFAULT_SCOPE and more
    fun openScopes(vararg scopes: String): Scope = if (scopes.isEmpty()) {
        Toothpick.openScopes(DEFAULT_SCOPE)
    } else {
        Toothpick.openScopes(*scopes)
    }

    fun close(vararg scopes: String) = scopes.forEach { Toothpick.closeScope(it) }
}

fun Any.objectScopeName() = "${javaClass.simpleName}_${hashCode()}"

fun <T> Fragment.getDependency(clazz: Class<T>, vararg scopes: String): T =
    DI.get(clazz, *scopes)
fun <T> FragmentActivity.getDependency(clazz: Class<T>, vararg scopes: String): T =
    DI.get(clazz, *scopes)

fun Fragment.injectDependencies(vararg scopes: String) = DI.inject(this, *scopes)
fun Fragment.injectDependencies(module: Module, vararg scopes: String) =
    DI.inject(this, arrayOf(module), *scopes)
fun Fragment.injectDependencies(modules: Array<out Module>, vararg scopes: String) =
    DI.inject(this, modules, *scopes)

fun FragmentActivity.injectDependencies(vararg scopes: String) = DI.inject(this, *scopes)
fun FragmentActivity.injectDependencies(module: Module, vararg scopes: String) =
    DI.inject(this, arrayOf(module), *scopes)
fun FragmentActivity.injectDependencies(modules: Array<out Module>, vararg scopes: String) =
    DI.inject(this, modules, *scopes)

fun Fragment.closeDependenciesScopes(vararg scopes: String) = DI.close(*scopes)
fun FragmentActivity.closeDependenciesScopes(vararg scopes: String) = DI.close(*scopes)

fun <T> Fragment.getScopedDependency(clazz: Class<T>): T = when (this) {
    is ScopeProvider -> getDependency(clazz, screenScopeTag)
    else -> getDependency(clazz)
}

fun <T> FragmentActivity.getScopedDependency(clazz: Class<T>): T = when (this) {
    is ScopeProvider -> getDependency(clazz, screenScopeTag)
    else -> getDependency(clazz)
}

inline fun <reified T : ViewModel> Fragment.viewModel(): Lazy<T> = lazy {
    ViewModelProviders
        .of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                this@viewModel.getScopedDependency(modelClass)
        })
        .get(T::class.java)
}

inline fun <reified T : ViewModel> FragmentActivity.viewModel(): Lazy<T> = lazy {
    ViewModelProviders
        .of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                this@viewModel.getScopedDependency(modelClass)
        })
        .get(T::class.java)
}

fun <T : Fragment> T.putScopeArgument(scope: String?): T {
    arguments = (arguments ?: Bundle()).apply {
        scope?.also {
            putString(ScopeProvider.ARG_PARENT_SCOPE, it)
        }
    }
    return this
}

fun Intent.putScopeArgument(scopesProvider: ScopeProvider?): Intent {
    scopesProvider?.screenScopeTag?.also {
        putExtra(ScopeProvider.ARG_PARENT_SCOPE, it)
    }
    return this
}

