package ru.radiationx.anilibria.common.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.leanback.app.GuidedStepSupportFragment
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.Back
import ru.terrakok.cicerone.commands.BackTo
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward
import ru.terrakok.cicerone.commands.Replace
import java.util.LinkedList
import kotlin.math.max

class GuidedStepNavigator(
    private val activity: FragmentActivity,
    containerId: Int,
    fragmentManager: FragmentManager = activity.supportFragmentManager,
) : SupportAppNavigator(activity, fragmentManager, containerId) {

    private val guidedStack = LinkedList<String>()

    private val backStack: List<FragmentManager.BackStackEntry>
        get() = (0 until fragmentManager.backStackEntryCount).map {
            fragmentManager.getBackStackEntryAt(
                it
            )
        }

    fun backStackById(id: Int): FragmentManager.BackStackEntry? = backStack.find { it.id == id }

    override fun setupFragmentTransaction(
        command: Command,
        currentFragment: Fragment?,
        nextFragment: Fragment?,
        fragmentTransaction: FragmentTransaction,
    ) {
        super.setupFragmentTransaction(command, currentFragment, nextFragment, fragmentTransaction)
        fragmentTransaction.setReorderingAllowed(true)
    }

    override fun applyCommands(commands: Array<out Command>) {
        val onlyGuidedCommands = commands.all { (it as? Forward)?.screen is GuidedAppScreen }
        if (onlyGuidedCommands) {
            for (command in commands) {
                applyCommand(command)
            }
        } else {
            super.applyCommands(commands)
        }
    }

    override fun applyCommand(command: Command) {
        when (command) {
            is Forward -> guidedForward(command)
            is Replace -> guidedReplace(command)
            is BackTo -> guidedBackTo(command)
            is Back -> guidedBack()
        }
    }

    private fun guidedForward(command: Forward) {
        if (command.screen is GuidedAppScreen) {
            val screen = command.screen as GuidedAppScreen
            val fragment =
                screen.fragment ?: throw RuntimeException("Can't create fragment for $screen")

            fragmentManager
                .beginTransaction()
                .also {
                    GuidedStepFragmentHelper.prepare(fragmentManager, it, fragment)
                }
                .replace(
                    android.R.id.content,
                    fragment,
                    GuidedStepFragmentHelper.TAG_LEAN_BACK_ACTIONS_FRAGMENT
                )
                .addToBackStack(screen.screenKey)
                .commit()

            guidedStack.add(screen.screenKey)
        } else {
            activityForward(command)
        }
    }

    private fun guidedReplace(command: Replace) {
        if (command.screen is GuidedAppScreen) {
            val screen = command.screen as GuidedAppScreen
            val fragment =
                screen.fragment ?: throw RuntimeException("Can't create fragment for $screen")

            val currentFragment =
                GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(fragmentManager)
            if (guidedStack.isNotEmpty() && currentFragment != null) {
                fragmentManager.popBackStackImmediate()
                guidedStack.removeLast()
            }

            fragmentManager
                .beginTransaction()
                .also {
                    GuidedStepFragmentHelper.prepare(fragmentManager, it, fragment)
                }
                .replace(
                    android.R.id.content,
                    fragment,
                    GuidedStepFragmentHelper.TAG_LEAN_BACK_ACTIONS_FRAGMENT
                )
                .addToBackStack(screen.screenKey)
                .commit()

            guidedStack.add(screen.screenKey)
        } else {
            activityReplace(command)
        }
    }

    private fun guidedBackTo(command: BackTo) {
        if (guidedStack.isNotEmpty()) {
            val key = command.screen?.screenKey
            val index = max(guidedStack.indexOf(key), 0)
            val range = (0 until guidedStack.size - index)
            repeat(range.count()) {
                guidedStack.removeLast()
                fragmentManager.popBackStack(key, 0)
            }
        } else {
            backTo(command)
        }
    }

    private fun guidedBack() {
        if (guidedStack.isNotEmpty()) {
            fragmentManager.popBackStack()
            guidedStack.removeLast()
        } else {
            fragmentBack()
        }
    }

}