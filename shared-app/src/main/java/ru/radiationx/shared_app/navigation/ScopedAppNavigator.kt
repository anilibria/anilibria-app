package ru.radiationx.shared_app.navigation

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import ru.radiationx.shared_app.di.ScopeProvider
import ru.radiationx.shared_app.di.putScopeArgument
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command

open class ScopedAppNavigator(
    private val activity: FragmentActivity,
    private val containerId: Int,
    private val fragmentManager: FragmentManager = activity.supportFragmentManager,
    private val scopeProvider: ScopeProvider
) : SupportAppNavigator(activity, fragmentManager, containerId) {

    override fun setupFragmentTransaction(
        command: Command?,
        currentFragment: Fragment?,
        nextFragment: Fragment?,
        fragmentTransaction: FragmentTransaction
    ) {
        super.setupFragmentTransaction(command, currentFragment, nextFragment, fragmentTransaction)
        fragmentTransaction.setReorderingAllowed(true)
        nextFragment?.putScopeArgument(scopeProvider.screenScopeTag)
    }

    override fun createStartActivityOptions(command: Command, activityIntent: Intent): Bundle? {
        activityIntent.putScopeArgument(scopeProvider)
        return super.createStartActivityOptions(command, activityIntent)
    }
}