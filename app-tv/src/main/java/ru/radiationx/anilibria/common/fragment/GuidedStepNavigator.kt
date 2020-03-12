package ru.radiationx.anilibria.common.fragment

import android.os.Handler
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.leanback.app.GuidedStepSupportFragment
import ru.radiationx.shared_app.di.ScopeProvider
import ru.radiationx.shared_app.di.putScopeArgument
import ru.radiationx.shared_app.navigation.ScopedAppNavigator
import ru.terrakok.cicerone.commands.*
import java.util.*
import kotlin.math.max

class GuidedStepNavigator(
    private val activity: FragmentActivity,
    private val containerId: Int,
    private val fragmentManager: FragmentManager = activity.supportFragmentManager,
    private val scopeProvider: ScopeProvider
) : ScopedAppNavigator(activity, containerId, fragmentManager, scopeProvider) {

    private val dialogStack = LinkedList<String>()

    private val activityFragmentManager = activity.supportFragmentManager

    private val backStack: List<FragmentManager.BackStackEntry>
        get() = (0 until fragmentManager.backStackEntryCount).map { fragmentManager.getBackStackEntryAt(it) }

    fun backStackById(id: Int): FragmentManager.BackStackEntry? = backStack.find { it.id == id }

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
            is BackTo -> dialogBackTo(command)
            is Back -> dialogBack()
        }
    }

    protected fun dialogForward(command: Forward) {

        Log.e("GuidedStepNavigator", "dialogForward ${command.screen}")
        if (command.screen is DialogAppScreen) {
            val screen = command.screen as DialogAppScreen
            val fragment = screen.fragment ?: throw RuntimeException("Can't create fragment for $screen")

            fragment.putScopeArgument(scopeProvider.screenScopeTag)

            activityFragmentManager
                .beginTransaction()
                .also {
                    GuidedStepFragmentHelper.prepare(activity.supportFragmentManager, it, fragment)
                }
                .replace(android.R.id.content, fragment, GuidedStepFragmentHelper.TAG_LEAN_BACK_ACTIONS_FRAGMENT)
                .addToBackStack(screen.screenKey)
                .commit()

            dialogStack.add(screen.screenKey)
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
            if (dialogStack.isNotEmpty() && currentFragment != null) {
                fragmentManager.popBackStackImmediate()
                dialogStack.removeLast()
            }

            activityFragmentManager
                .beginTransaction()
                .also {
                    GuidedStepFragmentHelper.prepare(activity.supportFragmentManager, it, fragment)
                }
                .replace(android.R.id.content, fragment, GuidedStepFragmentHelper.TAG_LEAN_BACK_ACTIONS_FRAGMENT)
                .addToBackStack(screen.screenKey)
                .commit()

            dialogStack.add(screen.screenKey)
        } else {
            activityReplace(command)
        }
    }

    protected fun dialogBackTo(command: BackTo) {
        if (dialogStack.isNotEmpty()) {
            val key = command.screen?.screenKey
            val index = max(dialogStack.indexOf(key), 0)
            val range = (0 until dialogStack.size - index)
            Log.e("GuidedStepNavigator", "dialogBackTo $key, $index, ${dialogStack.size}, $range")
            range.forEach {
                Log.e("GuidedStepNavigator", "dialogBackTo remove $it")
                dialogStack.removeLast()
                fragmentManager.popBackStack(key, 0)
            }
        } else {
            backTo(command)
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