package taiwa.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.res.use
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.viewbinding.ViewBinding
import com.google.android.material.internal.EdgeToEdgeUtils
import taiwa.common.ViewTransition

@SuppressLint("RestrictedApi")
abstract class BaseTaiwaDialog @JvmOverloads constructor(
    context: Context,
    @StyleRes theme: Int = 0,
) : AppCompatDialog(context, theme) {

    protected var _cancelable: Boolean = true
    private var _canceledOnTouchOutside = true
    private var _canceledOnTouchOutsideSet = false

    protected abstract val views: Views

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initEdgeToEdge()
        initInsets()
    }

    override fun setCancelable(cancelable: Boolean) {
        super.setCancelable(cancelable)
        _cancelable = cancelable
    }

    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        super.setCanceledOnTouchOutside(cancel)
        if (cancel && !_cancelable) {
            _cancelable = true
        }
        _canceledOnTouchOutside = cancel
        _canceledOnTouchOutsideSet = true
    }

    final override fun setContentView(view: View) {
        super.setContentView(wrapContentView(0, view, null))
    }

    final override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        super.setContentView(wrapContentView(0, view, params))
    }

    final override fun setContentView(layoutResID: Int) {
        super.setContentView(wrapContentView(layoutResID, null, null))
    }

    fun setFooterView(view: View) {
        super.setContentView(wrapFooterView(0, view, null))
    }

    fun setFooterView(view: View, params: ViewGroup.LayoutParams?) {
        super.setContentView(wrapFooterView(0, view, params))
    }

    fun setFooterView(layoutResID: Int) {
        super.setContentView(wrapFooterView(layoutResID, null, null))
    }

    final override fun addContentView(view: View, params: ViewGroup.LayoutParams?) {
        error("addContentView is unsupported in this implementation")
    }

    fun <T : View> setContentView(block: ((context: Context) -> T)): T {
        val view = block.invoke(views.contentContainer.context)
        setContentView(view)
        return view
    }

    fun <T : ViewBinding> setContentBinding(block: ((inflater: LayoutInflater) -> T)): T {
        val sheetBinding = block.invoke(LayoutInflater.from(views.contentContainer.context))
        setContentView(sheetBinding.root)
        return sheetBinding
    }

    fun <T : View> setFooterView(block: ((context: Context) -> T)): T {
        val view = block.invoke(views.footerContainer.context)
        setFooterView(view)
        return view
    }

    fun <T : ViewBinding> setFooterBinding(block: ((inflater: LayoutInflater) -> T)): T {
        val footerBinding = block.invoke(LayoutInflater.from(views.footerContainer.context))
        setFooterView(footerBinding.root)
        return footerBinding
    }

    abstract fun setFooterVisible(value: Boolean)

    protected abstract fun prepareContentView()

    protected abstract fun prepareFooterView()

    protected abstract fun applyWrapperInsets(wrapperInsets: Insets)

    fun prepareViewTransition(): ViewTransition {
        return ViewTransition(views.transitionRoot, this)
    }

    fun beginViewTransition(transition: ViewTransition) {
        if (!transition.prepared) return
        TransitionManager.beginDelayedTransition(views.transitionRoot, TransitionSet().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
            addTransition(Fade())
            addTransition(ChangeBounds())
        })
    }

    private fun wrapContentView(
        layoutResId: Int,
        view: View?,
        params: ViewGroup.LayoutParams?,
    ): View = wrapView(views.contentContainer, layoutResId, view, params) {
        prepareContentView()
    }

    private fun wrapFooterView(
        layoutResId: Int,
        view: View?,
        params: ViewGroup.LayoutParams?,
    ): View = wrapView(views.footerContainer, layoutResId, view, params) {
        prepareFooterView()
    }

    private fun wrapView(
        container: ViewGroup,
        layoutResId: Int,
        view: View?,
        params: ViewGroup.LayoutParams?,
        clearBlock: () -> Unit,
    ): View {
        val newView = when {
            layoutResId != 0 -> layoutInflater.inflate(layoutResId, container, false)
            view != null -> view
            else -> null
        }
        requireNotNull(newView) {
            "Valid view not found $layoutResId, $view, $params"
        }

        clearBlock.invoke()

        if (params != null) {
            container.addView(newView, params)
        } else {
            container.addView(newView)
        }

        return views.root
    }

    fun isCancelable(): Boolean {
        return _cancelable
    }

    fun isCanceledOnTouchOutside(): Boolean {
        if (!_canceledOnTouchOutsideSet) {
            val attrs = intArrayOf(android.R.attr.windowCloseOnTouchOutside)
            _canceledOnTouchOutside = context.obtainStyledAttributes(attrs).use {
                it.getBoolean(0, true)
            }
            _canceledOnTouchOutsideSet = true
        }
        return _canceledOnTouchOutside
    }

    fun isNeedHandleTouchOutside(): Boolean {
        return isCancelable() && isShowing && isCanceledOnTouchOutside()
    }

    fun requireWindow(): Window = requireNotNull(window) {
        "Window is null"
    }

    private fun initInsets() {
        requireWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        var isAnimationRunning = false
        ViewCompat.setOnApplyWindowInsetsListener(views.root) { _, insets ->
            if (!isAnimationRunning) {
                TransitionManager.endTransitions(views.transitionRoot)
                applyWrapperInsets(calculateWrapperInsets(insets))
            }
            insets
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return
        }
        ViewCompat.setWindowInsetsAnimationCallback(
            views.root,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {

                override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                    super.onPrepare(animation)
                    TransitionManager.endTransitions(views.transitionRoot)
                    isAnimationRunning = true
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>,
                ): WindowInsetsCompat {
                    applyWrapperInsets(calculateWrapperInsets(insets))
                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    super.onEnd(animation)
                    isAnimationRunning = false
                }
            }
        )
    }

    private fun calculateWrapperInsets(windowInsets: WindowInsetsCompat): Insets {
        val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        val cutout = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
        val horizontalCutout = maxOf(cutout.left, cutout.right)
        val basicInsets = Insets.of(
            maxOf(systemBars.left, horizontalCutout),
            maxOf(systemBars.top, cutout.top),
            maxOf(systemBars.right, horizontalCutout),
            maxOf(systemBars.bottom, cutout.bottom)
        )
        val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
        return Insets.max(basicInsets, imeInsets)
    }

    private fun initEdgeToEdge() {
        val window = requireWindow()
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        EdgeToEdgeUtils.applyEdgeToEdge(window, true, Color.BLACK, Color.BLACK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            window.attributes.layoutInDisplayCutoutMode = mode
        }
    }

    protected class Views(
        val root: ViewGroup,
        val transitionRoot: ViewGroup,
        val contentContainer: ViewGroup,
        val footerContainer: ViewGroup,
    )
}