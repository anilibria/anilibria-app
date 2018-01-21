package ru.radiationx.anilibria.ui.fragments;

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.design.widget.CollapsingToolbarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.common.BackButtonListener

/* Created by radiationx on 18.11.17. */

abstract class BaseFragment : MvpAppCompatFragment(), BackButtonListener {

    private val dimensionsProvider = App.injections.dimensionsProvider
    private var dimensionsDisposable: Disposable? = null

    @LayoutRes
    abstract protected fun getLayoutResource(): Int

    @LayoutRes
    open protected fun getBaseLayout(): Int = R.layout.fragment_main_base

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val newView: View? = inflater.inflate(getBaseLayout(), container, false)
        if (getLayoutResource() != View.NO_ID) {
            inflater.inflate(getLayoutResource(), newView?.findViewById(R.id.fragment_content), true)
        }
        return newView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dimensionsDisposable = dimensionsProvider.dimensions().subscribe {
            toolbar?.post {
                toolbar?.layoutParams = (toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams).apply {
                    topMargin = it.statusBar
                }
            }
        }
    }

    fun setStatusBarColor(color: Int) {
        baseStatusBar.setBackgroundColor(color)
    }

    fun setStatusBarVisibility(isVisible: Boolean) {
        baseStatusBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        dimensionsDisposable?.dispose()
    }
}
