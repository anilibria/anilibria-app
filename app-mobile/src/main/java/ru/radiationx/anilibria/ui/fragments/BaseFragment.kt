package ru.radiationx.anilibria.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.updateLayoutParams
import androidx.viewbinding.ViewBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.appbar.CollapsingToolbarLayout
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentMainBaseBinding
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.radiationx.shared.ktx.android.visible

/* Created by radiationx on 18.11.17. */

abstract class BaseFragment<T : ViewBinding>(
    @LayoutRes val contentLayoutId: Int
) : ScopeFragment(R.layout.fragment_main_base) {

    protected open val needToolbarShadow = true

    protected open val statusBarVisible = false

    protected val binding by viewBinding(vbFactory = {
        onCreateBinding(it)
    }, viewBindingRootId = R.id.fragment_content)

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

    override fun interceptDimens(dimensions: DimensionHelper.Dimensions) {
        val correctedDimens = dimensions.copy()
        baseBinding.baseStatusBar.apply {
            layoutParams = layoutParams.apply {
                height = correctedDimens.statusBar
            }
        }
        if (statusBarVisible) {
            correctedDimens.statusBar = 0
        }
        super.interceptDimens(correctedDimens)
    }

    override fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        super.updateDimens(dimensions)
        baseBinding.toolbar.updateLayoutParams<CollapsingToolbarLayout.LayoutParams> {
            topMargin = dimensions.statusBar
        }
    }

    protected fun updateToolbarShadow(isVisible: Boolean) {
        baseBinding.toolbarShadowPrelp.visible(isVisible && needToolbarShadow)
    }

    private fun setStatusBarVisibility(isVisible: Boolean) {
        baseBinding.baseStatusBar.visible(isVisible)
    }

}
