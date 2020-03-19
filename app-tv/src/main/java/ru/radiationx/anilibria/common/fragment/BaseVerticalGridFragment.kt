package ru.radiationx.anilibria.common.fragment

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.VerticalGridPresenter
import ru.radiationx.anilibria.LinkCard
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LoadingCard
import ru.radiationx.anilibria.common.MockData
import ru.radiationx.anilibria.common.fragment.scoped.ScopedVerticalGridFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.screen.main.toCard
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.anilibria.ui.widget.CardDescriptionView
import javax.inject.Inject
import kotlin.math.max

open class BaseVerticalGridFragment : ScopedVerticalGridFragment() {

    private var cardDescriptionView: CardDescriptionView? = null
    private var descriptionShadowView: FrameLayout? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dockView = view.findViewById<ViewGroup>(androidx.leanback.R.id.grid_frame)
        val gridView = view.findViewById<ViewGroup>(androidx.leanback.R.id.browse_grid)
        val shadowView = FrameLayout(dockView.context)
        val cardDescriptionView = CardDescriptionView(view.context, defStyleAttr = R.attr.itemsVerticalDescriptionStyle)

        this.descriptionShadowView = shadowView
        this.cardDescriptionView = cardDescriptionView

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
        shadowView.addView(
            cardDescriptionView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL
            )
        )

        gridView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
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
        cardDescriptionView = null
        descriptionShadowView = null
    }

    override fun runEntranceTransition(entranceTransition: Any?) {
        super.runEntranceTransition(entranceTransition)
        descriptionShadowView?.isVisible = true
    }

    protected fun setDescription(title: CharSequence, subtitle: CharSequence) {
        cardDescriptionView?.apply {
            setTitle(title)
            setSubtitle(subtitle)
            isVisible = isFilled()
        }

        cardDescriptionView?.post {
            cardDescriptionView?.requestLayout()
        }
    }

}