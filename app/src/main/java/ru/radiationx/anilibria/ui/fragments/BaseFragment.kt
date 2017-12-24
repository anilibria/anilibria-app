package ru.radiationx.anilibria.ui.fragments;

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.common.BackButtonListener

/* Created by radiationx on 18.11.17. */

abstract class BaseFragment : MvpAppCompatFragment(), BackButtonListener {

    @LayoutRes
    abstract protected fun getLayoutResource(): Int

    @LayoutRes
    protected fun getBaseLayout(): Int = R.layout.fragment_main_base

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val newView: View? = inflater.inflate(getBaseLayout(), container, false)
        inflater.inflate(getLayoutResource(), newView?.findViewById(R.id.fragment_content), true)
        return newView
    }
}
