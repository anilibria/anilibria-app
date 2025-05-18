package ru.radiationx.anilibria.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.CollapsingToolbarLayout
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentMainBaseBinding
import ru.radiationx.anilibria.utils.dimensions.Dimensions

/* Created by radiationx on 18.11.17. */

abstract class BaseLayoutFragment<BaseVB : ViewBinding, ContentVB : ViewBinding>(
    @LayoutRes private val baseLayoutId: Int,
    @LayoutRes private val contentLayoutId: Int
) : BaseDimensionsFragment(baseLayoutId) {

    protected open val needToolbarShadow = true

    protected open val statusBarVisible = false

    private val toolbarBinding by viewBinding<FragmentMainBaseBinding>()

    protected val baseBinding by viewBinding(vbFactory = {
        onCreateBaseBinding(it)
    })

    protected val binding by viewBinding(vbFactory = {
        onCreateBinding(it)
    }, viewProvider = {
        ViewCompat
            .requireViewById<FrameLayout>(requireView(), R.id.fragment_content)
            .getChildAt(0)
    })

    protected abstract fun onCreateBaseBinding(view: View): BaseVB

    protected abstract fun onCreateBinding(view: View): ContentVB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = super.onCreateView(inflater, container, savedInstanceState)?.also { view ->
        inflater.inflate(
            contentLayoutId,
            view.findViewById(R.id.fragment_content),
            true
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStatusBarVisibility(statusBarVisible)
    }

    override fun interceptDimens(dimensions: Dimensions) {
        toolbarBinding.baseStatusBar.updateLayoutParams {
            height = dimensions.top
        }
        val correctedDimens = if (statusBarVisible) {
            dimensions.copy(top = 0)
        } else {
            dimensions
        }
        super.interceptDimens(correctedDimens)
    }

    override fun updateDimens(dimensions: Dimensions) {
        super.updateDimens(dimensions)
        toolbarBinding.toolbar.updateLayoutParams<CollapsingToolbarLayout.LayoutParams> {
            leftMargin = dimensions.left
            topMargin = dimensions.top
            rightMargin = dimensions.right
        }
    }

    protected fun updateToolbarShadow(isVisible: Boolean) {
        toolbarBinding.toolbarShadowPrelp.isVisible = isVisible && needToolbarShadow
    }

    private fun setStatusBarVisibility(isVisible: Boolean) {
        toolbarBinding.baseStatusBar.isVisible = isVisible
    }

}
