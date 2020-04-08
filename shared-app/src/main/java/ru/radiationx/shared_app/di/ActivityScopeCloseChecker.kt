package ru.radiationx.shared_app.di

import androidx.fragment.app.FragmentActivity

class ActivityScopeCloseChecker(private val activity: FragmentActivity) : ScopeCloseChecker {

    override fun needCloseScope(): Boolean = activity.isFinishing

}