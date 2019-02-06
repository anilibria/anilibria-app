package ru.radiationx.anilibria.ui.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.LayoutRes
import android.support.design.widget.CollapsingToolbarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.arellomobile.mvp.MvpAppCompatFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.addTo
import ru.radiationx.anilibria.model.system.messages.SystemMessenger
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.ui.common.ScreenMessagesObserver
import ru.radiationx.anilibria.utils.DimensionHelper

/* Created by radiationx on 18.11.17. */

abstract class BaseFragment : MvpAppCompatFragment(), BackButtonListener {

    private val dimensionsProvider = App.injections.dimensionsProvider
    private val disposables = CompositeDisposable()
    private val screenMessagesObserver = ScreenMessagesObserver()
    protected val screenMessenger: SystemMessenger
        get() = screenMessagesObserver.screenMessenger

    protected open val needToolbarShadow = true

    @LayoutRes
    protected open fun getLayoutResource(): Int = View.NO_ID

    @LayoutRes
    protected open fun getBaseLayout(): Int = R.layout.fragment_main_base

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(screenMessagesObserver)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        screenMessagesObserver.context = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val newView: View? = inflater.inflate(getBaseLayout(), container, false)
        if (getLayoutResource() != View.NO_ID) {
            inflater.inflate(getLayoutResource(), newView?.findViewById(R.id.fragment_content), true)
        }
        return newView
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && needToolbarShadow) {
            toolbar_shadow_prelp?.visibility = View.VISIBLE
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dimensionsProvider
                .dimensions()
                .subscribe {
                    toolbar?.post {
                        toolbar?.let { _ ->
                            updateDimens(it)
                        }
                    }
                    updateDimens(it)
                }
                .addTo(disposables)
    }

    open fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        toolbar?.layoutParams = (toolbar?.layoutParams as CollapsingToolbarLayout.LayoutParams?)?.apply {
            topMargin = dimensions.statusBar
        }
    }

    fun setStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return
        }
        baseStatusBar?.setBackgroundColor(color)
    }

    fun setStatusBarVisibility(isVisible: Boolean) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            baseStatusBar?.visibility = View.GONE
            return
        }
        baseStatusBar?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    protected fun hideSoftwareKeyboard() {
        activity?.also {
            val inputManager = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            it.currentFocus?.let {
                inputManager?.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    protected fun showSoftwareKeyboard(editText: EditText) {
        activity?.also {
            val inputManager = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputManager?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }
}
