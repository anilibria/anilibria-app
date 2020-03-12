package ru.radiationx.anilibria.common.fragment

import androidx.fragment.app.Fragment
import androidx.leanback.app.GuidedStepSupportFragment
import ru.terrakok.cicerone.Screen
import ru.terrakok.cicerone.android.support.SupportAppScreen

open class DialogAppScreen : SupportAppScreen() {

    override fun getFragment(): GuidedStepFragmentHelper.AppGuidedStepSupportFragment? = null
}