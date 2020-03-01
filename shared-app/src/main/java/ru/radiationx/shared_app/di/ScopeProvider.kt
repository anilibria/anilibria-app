package ru.radiationx.shared_app.di

interface ScopeProvider {
    companion object {
        const val ARG_PARENT_SCOPE = "parent_scopes"
        const val STATE_SCREEN_SCOPE = "state_screen_scope"
    }

    val screenScopeTag: String
}