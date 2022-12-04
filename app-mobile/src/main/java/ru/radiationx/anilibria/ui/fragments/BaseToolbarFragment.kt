package ru.radiationx.anilibria.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.viewbinding.ViewBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.appbar.CollapsingToolbarLayout
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentMainBaseBinding
import ru.radiationx.anilibria.utils.Dimensions
import ru.radiationx.shared.ktx.android.visible

/* Created by radiationx on 18.11.17. */

abstract class BaseToolbarFragment<T : ViewBinding>(
    @LayoutRes private val contentLayoutId: Int
) : BaseDimensionsFragment(R.layout.fragment_main_base) {

    companion object {
        private const val CONTAINER_ID = R.id.fragment_content
    }

    protected open val needToolbarShadow = true

    protected open val statusBarVisible = false

    protected val binding by viewBinding(vbFactory = {
        onCreateBinding(it)
    }, viewProvider = {
        ViewCompat
            .requireViewById<FrameLayout>(requireView(), CONTAINER_ID)
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
            view.findViewById(CONTAINER_ID),
            true
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStatusBarVisibility(statusBarVisible)
    }

    override fun interceptDimens(dimensions: Dimensions) {
        baseBinding.baseStatusBar.updateLayoutParams {
            height = dimensions.statusBar
        }
        val correctedDimens = if (statusBarVisible) {
            dimensions.copy(statusBar = 0)
        } else {
            dimensions
        }
        super.interceptDimens(correctedDimens)
    }

    override fun updateDimens(dimensions: Dimensions) {
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
