package ru.radiationx.anilibria.ui.widget.manager

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView

class ExternalTextManager : ExternalViewManager() {

    var text: String = ""

    override fun setCreatedView(view: View) {
        require(view is TextView) { "Require TextView" }
        super.setCreatedView(view)
    }

    override fun onApplyShowing() {
        super.onApplyShowing()
        (externalView as? TextView?)?.text = text
    }

    override fun createView(): View {
        return TextView(rootView?.context)
    }

    override fun createLayoutParams(): ViewGroup.LayoutParams {
        val centerLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        centerLayoutParams.gravity = Gravity.CENTER
        return centerLayoutParams
    }
}