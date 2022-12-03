package ru.radiationx.anilibria.common.fragment

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.leanback.app.GuidedStepSupportFragment

object GuidedStepFragmentHelper {

    const val TAG = "GuidedStepF"
    const val TAG_LEAN_BACK_ACTIONS_FRAGMENT = "leanBackGuidedStepSupportFragment"
    private const val ENTRY_NAME_REPLACE = "GuidedStepDefault"
    private const val ENTRY_NAME_ENTRANCE = "GuidedStepEntrance"

    fun generateStackEntryName(fragment: GuidedStepSupportFragment): String =
        generateStackEntryName(fragment.uiStyle, fragment.javaClass)

    fun generateStackEntryName(uiStyle: Int, guidedStepFragmentClass: Class<*>): String =
        when (uiStyle) {
            GuidedStepSupportFragment.UI_STYLE_REPLACE -> ENTRY_NAME_REPLACE + guidedStepFragmentClass.name
            GuidedStepSupportFragment.UI_STYLE_ENTRANCE -> ENTRY_NAME_ENTRANCE + guidedStepFragmentClass.name
            GuidedStepSupportFragment.UI_STYLE_ACTIVITY_ROOT -> ""
            else -> ""
        }

    fun add(
        fragmentManager: FragmentManager,
        fragment: FakeGuidedStepFragment,
        containerId: Int = android.R.id.content
    ): Int {
        val current = GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(fragmentManager)
        val inGuidedStep = current != null
        val ft = fragmentManager.beginTransaction()

        fragment.uiStyle =
            if (inGuidedStep) GuidedStepSupportFragment.UI_STYLE_REPLACE else GuidedStepSupportFragment.UI_STYLE_ENTRANCE
        ft.addToBackStack(generateStackEntryName(fragment))
        if (current != null) {
            fragment.fakeOnAddSharedElementTransition(ft, current)
        }
        return ft.replace(containerId, fragment, TAG_LEAN_BACK_ACTIONS_FRAGMENT).commit()
    }

    fun prepare(
        fragmentManager: FragmentManager,
        fragmentTransaction: FragmentTransaction,
        fragment: FakeGuidedStepFragment
    ) {
        val current = GuidedStepSupportFragment.getCurrentGuidedStepSupportFragment(fragmentManager)
        val inGuidedStep = current != null

        fragment.uiStyle =
            if (inGuidedStep) GuidedStepSupportFragment.UI_STYLE_REPLACE else GuidedStepSupportFragment.UI_STYLE_ENTRANCE
        fragmentTransaction.addToBackStack(generateStackEntryName(fragment))
        if (current != null) {
            fragment.fakeOnAddSharedElementTransition(fragmentTransaction, current)
        }
    }


}