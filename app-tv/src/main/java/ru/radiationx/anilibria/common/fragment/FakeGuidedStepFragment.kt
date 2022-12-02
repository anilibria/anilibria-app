package ru.radiationx.anilibria.common.fragment

import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import androidx.leanback.app.GuidedStepSupportFragment
import ru.radiationx.quill.quillGet
import ru.radiationx.shared.ktx.android.attachBackPressed

open class FakeGuidedStepFragment : GuidedStepSupportFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachBackPressed {
            if (isEnabled) {
                quillGet<GuidedRouter>().exit()
                isEnabled = false
            }
        }
    }

    fun fakeOnAddSharedElementTransition(
        transaction: FragmentTransaction,
        disappearingFragment: GuidedStepSupportFragment
    ) {
        onAddSharedElementTransition(transaction, disappearingFragment)
    }
}