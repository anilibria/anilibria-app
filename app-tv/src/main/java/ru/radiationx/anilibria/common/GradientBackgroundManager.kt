package ru.radiationx.anilibria.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.BackgroundManager
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import com.google.android.material.animation.ArgbEvaluatorCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.radiationx.anilibria.R
import ru.radiationx.shared.ktx.android.asSoftware
import ru.radiationx.shared.ktx.android.getCompatColor
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.imageloader.loadImageBitmap
import timber.log.Timber
import javax.inject.Inject

class GradientBackgroundManager @Inject constructor(
    private val activity: FragmentActivity,
) {

    private val backgroundManager: BackgroundManager by lazy {
        BackgroundManager.getInstance(activity)
    }

    private val defaultColor = activity.getCompatColor(R.color.dark_colorAccent)
    private val foregroundColor = activity.getCompatColor(R.color.dark_windowBackground)
    private val loadingColor = ColorUtils.blendARGB(foregroundColor, defaultColor, 0.35f)

    private val backgroundDrawable = ColorDrawable(defaultColor)
    private val foregroundDrawable = ColorDrawable(foregroundColor)
    private val classicGradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.BL_TR,
        intArrayOf(
            Color.parseColor("#ee000000"),
            Color.parseColor("#55000000")
        )
    )
    private val customGradientDrawable = LinearGradientDrawable(
        190f,
        intArrayOf(
            Color.parseColor("#ee000000"),
            Color.parseColor("#55000000")
        )
    )
    private var defaultRevealStart: Runnable? = null
    private var defaultRevealPosted = false
    private val layerDrawable = object : LayerDrawable(
        arrayOf(
            backgroundDrawable, customGradientDrawable, foregroundDrawable
        )
    ) {
        override fun draw(canvas: Canvas) {
            super.draw(canvas)
            val revealStart = defaultRevealStart ?: return
            // Leanback can delay installation and temporarily change our alpha.
            // Start after an actual fully opaque neutral frame has been drawn.
            if (!defaultRevealPosted && foregroundDrawable.alpha == 255) {
                defaultRevealPosted = true
                activity.window.decorView.postOnAnimation(revealStart)
            }
        }
    }

    private var primaryColorAnimator: ValueAnimator? = null
    private var foregroundColorAnimator: ValueAnimator? = null
    private var defaultColorReveal: ValueAnimator? = null
    private var pendingRevealColor: Int? = null
    private var loadingBackgroundShown = false
    private var imageApplierJob: Job? = null
    private var colorApplierJob: Job? = null
    private val colorApplier = MutableStateFlow(defaultColor)
    private val colorEvaluator = ArgbEvaluatorCompat()
    private val urlColorMap = mutableMapOf<String, Int>()

    private val defaultColorSelector = { palette: Palette ->
        palette.getMutedColor(defaultColor)
    }

    private val defaultColorModifier = { color: Int -> color }

    init {
        if (!backgroundManager.isAttached) {
            backgroundManager.isAutoReleaseOnStop = false

            // to avoid java.lang.NullPointerException: Attempt to invoke virtual method 'android.graphics.drawable.Drawable android.graphics.drawable.Drawable$ConstantState.newDrawable()' on a null object reference
            // hope this helps
            backgroundManager.color = foregroundColor

            backgroundManager.attach(activity.window)
            backgroundManager.drawable = layerDrawable
        }
    }

    private fun subscribeColorApplier() {
        colorApplierJob?.cancel()
        colorApplierJob = colorApplier
            .debounce(200)
            .onEach {
                instantApplyColor(it)
            }
            .launchIn(activity.lifecycleScope)
    }

    fun clearGradient() {
        imageApplierJob?.cancel()
        colorApplierJob?.cancel()
        defaultRevealStart?.let { activity.window.decorView.removeCallbacks(it) }
        defaultRevealStart = null
        pendingRevealColor = null
        instantApplyForeground(true)
    }

    fun applyDefault() {
        applyColor(defaultColor)
    }

    fun showDefaultWhileLoading() {
        if (loadingBackgroundShown) return
        loadingBackgroundShown = true
        if (foregroundDrawable.alpha != 255) return
        imageApplierJob?.cancel()
        colorApplierJob?.cancel()
        primaryColorAnimator?.cancel()
        backgroundDrawable.color = loadingColor
        defaultRevealPosted = false
        defaultRevealStart = Runnable {
            defaultRevealStart = null
            startDefaultReveal()
        }
        layerDrawable.invalidateSelf()
    }

    private fun startDefaultReveal() {
        instantApplyForeground(false)
        val reveal = foregroundColorAnimator ?: return
        defaultColorReveal = reveal
        reveal.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                if (defaultColorReveal === animation) {
                    defaultColorReveal = null
                    pendingRevealColor = null
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                if (defaultColorReveal !== animation) return
                defaultColorReveal = null
                val nextColor = pendingRevealColor
                pendingRevealColor = null
                nextColor?.let { instantApplyColor(it) }
            }
        })
    }

    fun applyImage(
        url: String,
        colorSelector: (Palette) -> Int? = defaultColorSelector,
        colorModifier: (Int) -> Int = defaultColorModifier,
    ) {
        // A newly selected image supersedes any palette waiting for the reveal.
        pendingRevealColor = null
        colorApplierJob?.cancel()
        imageApplierJob?.cancel()
        val color = urlColorMap[url]
        if (colorSelector == defaultColorSelector && color != null) {
            applyColor(color, colorModifier)
            return
        }

        imageApplierJob = activity.lifecycleScope.launch {
            coRunCatching {
                val bitmap = withContext(Dispatchers.IO) {
                    activity.loadImageBitmap(url)
                } ?: return@coRunCatching null
                withContext(Dispatchers.Default) {
                    bitmap.asSoftware {
                        Palette.Builder(it).generate()
                    }
                }
            }.onSuccess { palette ->
                if (palette == null) {
                    applyDefault()
                    return@onSuccess
                }
                if (colorSelector == defaultColorSelector) {
                    urlColorMap[url] = colorSelector(palette) ?: defaultColorSelector(palette)
                }
                applyPalette(palette, colorSelector, colorModifier)
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun applyPalette(
        palette: Palette,
        colorSelector: (Palette) -> Int? = defaultColorSelector,
        colorModifier: (Int) -> Int = defaultColorModifier,
    ) {
        applyColor(colorSelector(palette) ?: defaultColorSelector(palette), colorModifier)
    }

    private fun applyColor(
        @ColorInt color: Int,
        colorModifier: (Int) -> Int = defaultColorModifier,
    ) {
        val finalColor = colorModifier.invoke(color)
        subscribeColorApplier()
        colorApplier.value = finalColor
    }

    private fun instantApplyColor(@ColorInt color: Int) {
        if (defaultRevealStart != null || defaultColorReveal?.isRunning == true) {
            pendingRevealColor = color
            return
        }
        imageApplierJob?.cancel()
        primaryColorAnimator?.cancel()
        if (foregroundDrawable.alpha == 255) {
            // Prepare the target while the neutral foreground still covers it.
            // Revealing and recoloring together would expose the previous color.
            backgroundDrawable.color = color
            instantApplyForeground(false)
            return
        }
        if (foregroundDrawable.alpha != 0) {
            instantApplyForeground(false)
        }
        primaryColorAnimator = ValueAnimator
            .ofObject(colorEvaluator, backgroundDrawable.color, color)
            .apply {
                duration = 500
                addUpdateListener {
                    backgroundDrawable.color = it.animatedValue as Int
                }
                start()
            }
    }

    private fun instantApplyForeground(visible: Boolean) {
        val start = foregroundDrawable.alpha
        val end = if (visible) 255 else 0
        foregroundColorAnimator?.cancel()
        foregroundColorAnimator = ValueAnimator
            .ofInt(start, end)
            .apply {
                duration = 500
                addUpdateListener {
                    foregroundDrawable.alpha = it.animatedValue as Int
                }
                start()
            }
    }
}
