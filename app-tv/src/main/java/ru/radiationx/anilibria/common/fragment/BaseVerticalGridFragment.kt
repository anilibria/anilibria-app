package ru.radiationx.anilibria.common.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.fragment.scoped.ScopedVerticalGridFragment
import ru.radiationx.anilibria.ui.widget.ShadowDescriptionView
import kotlin.math.max

open class BaseVerticalGridFragment : ScopedVerticalGridFragment() {

    private var shadowDescriptionView: ShadowDescriptionView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dockView = view.findViewById<ViewGroup>(androidx.leanback.R.id.grid_frame)
        val gridView = view.findViewById<ViewGroup>(androidx.leanback.R.id.browse_grid)
        val shadowView = ShadowDescriptionView(dockView.context)

        this.shadowDescriptionView = shadowView

        shadowView.setBackgroundResource(R.drawable.bg_grid_description_shadow)
        shadowView.isInvisible = true

        dockView.addView(
            shadowView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            )
        )

        gridView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val cardDescriptionView = shadowDescriptionView?.getCardDescriptionView() ?: return@addOnLayoutChangeListener
            val newWidth = max(v.width - v.paddingLeft - v.paddingRight, cardDescriptionView.minimumWidth)
            val currentWidth = cardDescriptionView.layoutParams.width
            if (currentWidth != newWidth) {
                cardDescriptionView.updateLayoutParams {
                    width = newWidth
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        shadowDescriptionView = null
    }

    protected fun setDescriptionVisible(visible: Boolean) {
        shadowDescriptionView?.isInvisible = !visible
    }

    protected fun setDescription(title: CharSequence, subtitle: CharSequence) {
        shadowDescriptionView?.setDescription(title, subtitle)
    }

}