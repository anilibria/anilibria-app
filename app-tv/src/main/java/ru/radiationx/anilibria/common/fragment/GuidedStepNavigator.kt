package ru.radiationx.anilibria.common.fragment

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

    private val guidedStack = LinkedList<String>()

    private val activityFragmentManager = activity.supportFragmentManager

    private val backStack: List<FragmentManager.BackStackEntry>
        get() = (0 until fragmentManager.backStackEntryCount).map { fragmentManager.getBackStackEntryAt(it) }

    fun backStackById(id: Int): FragmentManager.BackStackEntry? = backStack.find { it.id == id }

    override fun applyCommands(commands: Array<out Command>) {
        val onlyGuidedCommands = commands.all { (it as? Forward)?.screen is GuidedAppScreen }
        Log.e("GuidedStepNavigator", "applyCommands only $onlyGuidedCommands")
        if (onlyGuidedCommands) {
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
            is Forward -> guidedForward(command)
            is Replace -> guidedReplace(command)
            is BackTo -> guidedBackTo(command)
            is Back -> guidedBack()
        }
    }

    protected fun guidedForward(command: Forward) {

        Log.e("GuidedStepNavigator", "guidedForward ${command.screen}")
        if (command.screen is GuidedAppScreen) {
            val screen = command.screen as GuidedAppScreen
            val fragment = screen.fragment ?: throw RuntimeException("Can't create fragment for $screen")

            fragment.putScopeArgument(getActualScopeProvider().screenScopeTag)

            activityFragmentManager
                .beginTransaction()
                .also {
                    GuidedStepFragmentHelper.prepare(activity.supportFragmentManager, it, fragment)
                }
                .replace(android.R.id.content, fragment, GuidedStepFragmentHelper.TAG_LEAN_BACK_ACTIONS_FRAGMENT)
                .addToBackStack(screen.screenKey)
                .commit()

            guidedStack.add(screen.screenKey)
        } else {
            activityForward(command)
        }
    }

    protected fun guidedReplace(command: Replace) {

        Log.e("GuidedStepNavigator", "guidedForward ${command.screen}")
        if (command.screen is GuidedAppScreen) {
            val screen = command.screen as GuidedAppScreen
            val fragment = screen.fragment ?: throw RuntimeException("Can't create fragment for $screen")

            fragment.putScopeArgument(getActualScopeProvider().screenScopeTag)

            val currentFragment = GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(fragmentManager)
            if (guidedStack.isNotEmpty() && currentFragment != null) {
                fragmentManager.popBackStackImmediate()
                guidedStack.removeLast()
            }

            activityFragmentManager
                .beginTransaction()
                .also {
                    GuidedStepFragmentHelper.prepare(activity.supportFragmentManager, it, fragment)
                }
                .replace(android.R.id.content, fragment, GuidedStepFragmentHelper.TAG_LEAN_BACK_ACTIONS_FRAGMENT)
                .addToBackStack(screen.screenKey)
                .commit()

            guidedStack.add(screen.screenKey)
        } else {
            activityReplace(command)
        }
    }

    protected fun guidedBackTo(command: BackTo) {
        if (guidedStack.isNotEmpty()) {
            val key = command.screen?.screenKey
            val index = max(guidedStack.indexOf(key), 0)
            val range = (0 until guidedStack.size - index)
            Log.e("GuidedStepNavigator", "guidedBackTo $key, $index, ${guidedStack.size}, $range")
            range.forEach {
                Log.e("GuidedStepNavigator", "guidedBackTo remove $it")
                guidedStack.removeLast()
                fragmentManager.popBackStack(key, 0)
            }
        } else {
            backTo(command)
        }
    }

    protected fun guidedBack() {
        val currentFragment = GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(fragmentManager)

        Log.e(
            "GuidedStepNavigator",
            "guidedBack old current = $currentFragment, stack = ${(0 until fragmentManager.backStackEntryCount).map {
                fragmentManager.getBackStackEntryAt(it)
            }.joinToString()}"
        )
        Log.e(
            "GuidedStepNavigator",
            "guidedBack new current = $currentFragment, stack = ${guidedStack.joinToString()}"
        )
        if (guidedStack.isNotEmpty()) {
            fragmentManager.popBackStack()
            guidedStack.removeLast()
        } else {
            fragmentBack()
        }
    }

    private fun getActualScopeProvider(): ScopeProvider {
        // todo Работает не стабильно, т.к. может использоваться старый Scope, который к моменту создания экрана уже будет уничтожен
        /*val fragment = fragmentManager.findFragmentById(containerId) as? ScopeProvider?
        return fragment ?: scopeProvider*/
        return scopeProvider
    }
}