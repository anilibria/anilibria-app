package ru.radiationx.anilibria.common.fragment

import androidx.fragment.app.FragmentTransaction
import androidx.leanback.app.GuidedStepSupportFragment

open class FakeGuidedStepFragment : GuidedStepSupportFragment() {

    fun fakeOnAddSharedElementTransition(transaction: FragmentTransaction, disappearingFragment: GuidedStepSupportFragment) {
        onAddSharedElementTransition(transaction, disappearingFragment)
    }
}