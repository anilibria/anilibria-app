package ru.radiationx.anilibria.ui.fragments

import android.view.View
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentMainBaseBinding

/* Created by radiationx on 18.11.17. */

abstract class BaseToolbarFragment<ContentVB : ViewBinding>(
    @LayoutRes private val contentLayoutId: Int
) : BaseLayoutFragment<FragmentMainBaseBinding, ContentVB>(
    baseLayoutId = R.layout.fragment_main_base,
    contentLayoutId = contentLayoutId
) {

    override fun onCreateBaseBinding(view: View): FragmentMainBaseBinding {
        return FragmentMainBaseBinding.bind(view)
    }
}
