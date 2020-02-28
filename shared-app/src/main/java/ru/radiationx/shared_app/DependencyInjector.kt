package ru.radiationx.shared_app

import android.os.Bundle
import toothpick.Scope
import toothpick.Toothpick
import toothpick.config.Module

class DependencyInjector() {

    private val localModules = mutableListOf<Module>()

    var screenScopeTag: String = objectScopeName()

    fun installModules(vararg module: Module) {
        localModules.addAll(module)
    }

    fun onCreate(target: Any, arguments: Bundle?, savedInstanceState: Bundle?): Scope {
        val parentScopeTag: String = arguments?.getString(ScopeProvider.ARG_PARENT_SCOPE) ?: DI.DEFAULT_SCOPE
        screenScopeTag = savedInstanceState?.getString(ScopeProvider.STATE_SCREEN_SCOPE) ?: screenScopeTag

        return if (savedInstanceState == null || !Toothpick.isScopeOpen(screenScopeTag)) {
            DI.inject(target, localModules.toTypedArray(), parentScopeTag, screenScopeTag)
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