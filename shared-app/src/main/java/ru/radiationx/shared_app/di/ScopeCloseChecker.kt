package ru.radiationx.shared_app.di

interface ScopeCloseChecker {
    fun needCloseScope(): Boolean
}