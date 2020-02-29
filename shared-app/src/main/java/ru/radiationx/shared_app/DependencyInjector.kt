package ru.radiationx.shared_app

import android.os.Bundle
import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Module

class DependencyInjector(arguments: Bundle?) {

    private val modules = mutableListOf<Module>()

    val parentScopeTag: String = arguments?.getString(ScopeProvider.ARG_PARENT_SCOPE) ?: DI.DEFAULT_SCOPE

    var screenScopeTag: String = objectScopeName()
        private set

    fun installModules(vararg module: Module) {
        modules.addAll(module)
    }

    fun onCreate(target: Any, savedInstanceState: Bundle?): Scope {
        screenScopeTag = savedInstanceState?.getString(ScopeProvider.STATE_SCREEN_SCOPE) ?: screenScopeTag
        return if (savedInstanceState == null || !Toothpick.isScopeOpen(screenScopeTag)) {
            DI.inject(target, modules.toTypedArray(), parentScopeTag, screenScopeTag)
        } else {
            DI.inject(target, screenScopeTag)
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putString(ScopeProvider.STATE_SCREEN_SCOPE, screenScopeTag)
    }

    fun closeScope() {
        DI.close(screenScopeTag)
    }
}