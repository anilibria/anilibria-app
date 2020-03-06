package ru.radiationx.anilibria.common.fragment

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.leanback.app.GuidedStepSupportFragment
import ru.radiationx.shared_app.di.ScopeProvider
import ru.radiationx.shared_app.di.putScopeArgument
import ru.radiationx.shared_app.navigation.ScopedAppNavigator
import ru.terrakok.cicerone.commands.*

class GuidedStepNavigator(
    private val activity: FragmentActivity,
    private val containerId: Int,
    private val fragmentManager: FragmentManager = activity.supportFragmentManager,
    private val scopeProvider: ScopeProvider
) : ScopedAppNavigator(activity, containerId, fragmentManager, scopeProvider) {

    override fun applyCommands(commands: Array<out Command>) {
        val onlyDialogCommands = commands.all { (it as? Forward)?.screen is DialogAppScreen }
        Log.e("GuidedStepNavigator", "applyCommands only $onlyDialogCommands")
        if (onlyDialogCommands) {
            for (command in commands) {
                applyCommand(command)
            }
        } else {
            super.applyCommands(commands)
        }
    }

    override fun applyCommand(command: Command?) {

        Log.e("GuidedStepNavigator", "applyCommand $command")
        when (command) {
            is Forward -> dialogForward(command)
            is Replace -> dialogReplace(command)
            is BackTo -> backTo(command)
            is Back -> dialogBack()
        }
    }

    protected fun dialogForward(command: Forward) {

        Log.e("GuidedStepNavigator", "dialogForward ${command.screen}")
        if (command.screen is DialogAppScreen) {
            val screen = command.screen as DialogAppScreen
            val fragment = screen.fragment ?: throw RuntimeException("Can't create fragment for $screen")
            val currentFragment = GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(fragmentManager)

            fragment.putScopeArgument(scopeProvider.screenScopeTag)

            GuidedStepSupportFragment.add(activity.supportFragmentManager, fragment, android.R.id.content)
        } else {
            activityForward(command)
        }
    }

    protected fun dialogReplace(command: Replace) {

        Log.e("GuidedStepNavigator", "dialogForward ${command.screen}")
        if (command.screen is DialogAppScreen) {
            val screen = command.screen as DialogAppScreen
            val fragment = screen.fragment ?: throw RuntimeException("Can't create fragment for $screen")

            fragment.putScopeArgument(scopeProvider.screenScopeTag)

            val currentFragment = GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(fragmentManager)

            if (currentFragment != null) {
                fragmentManager.popBackStackImmediate()
            }

            GuidedStepSupportFragment.add(activity.supportFragmentManager, fragment, android.R.id.content)
        } else {
            activityReplace(command)
        }
    }


    protected fun dialogBack() {
        val currentFragment = GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(fragmentManager)

        Log.e(
            "GuidedStepNavigator",
            "dialogBack current = $currentFragment, stack = ${(0 until fragmentManager.backStackEntryCount).map {
                fragmentManager.getBackStackEntryAt(it)
            }.joinToString()}"
        )
        if (currentFragment != null) {
            fragmentManager.popBackStack()
        } else {
            fragmentBack()
        }
    }
}