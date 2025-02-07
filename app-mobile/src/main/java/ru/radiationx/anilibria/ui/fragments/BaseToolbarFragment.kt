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
import dev.androidbroadcast.vbpd.viewBinding
import com.google.android.material.appbar.CollapsingToolbarLayout
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentMainBaseBinding
import ru.radiationx.anilibria.utils.dimensions.Dimensions

/* Created by radiationx on 18.11.17. */

abstract class BaseToolbarFragment<T : ViewBinding>(
    @LayoutRes private val contentLayoutId: Int
) : BaseDimensionsFragment(R.layout.fragment_main_base) {

    protected open val needToolbarShadow = true

    protected open val statusBarVisible = false

    protected val binding by viewBinding(vbFactory = {
        onCreateBinding(it)
    }, viewProvider = {
        ViewCompat
            .requireViewById<FrameLayout>(requireView(), R.id.fragment_content)
            .getChildAt(0)
    })

    protected val baseBinding by viewBinding<FragmentMainBaseBinding>()

    protected abstract fun onCreateBinding(view: View): T

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
        baseBinding.baseStatusBar.updateLayoutParams {
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
        baseBinding.toolbar.updateLayoutParams<CollapsingToolbarLayout.LayoutParams> {
            leftMargin = dimensions.left
            topMargin = dimensions.top
            rightMargin = dimensions.right
        }
    }

    protected fun updateToolbarShadow(isVisible: Boolean) {
        baseBinding.toolbarShadowPrelp.isVisible = isVisible && needToolbarShadow
    }

    private fun setStatusBarVisibility(isVisible: Boolean) {
        baseBinding.baseStatusBar.isVisible = isVisible
    }

}
