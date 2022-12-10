package ru.radiationx.anilibria.common

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.ColorInt
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
import ru.radiationx.shared.ktx.android.getCompatColor
import ru.radiationx.shared.ktx.android.asSoftware
import ru.radiationx.shared_app.imageloader.loadImageBitmap
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class GradientBackgroundManager(
    private val activity: FragmentActivity
) {

    private val backgroundManager: BackgroundManager by lazy {
        BackgroundManager.getInstance(activity)
    }

    private val defaultColor = activity.getCompatColor(R.color.dark_colorAccent)
    private val foregroundColor = activity.getCompatColor(R.color.dark_windowBackground)

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
    private val layerDrawable = LayerDrawable(
        arrayOf(
            backgroundDrawable, customGradientDrawable, foregroundDrawable
        )
    )

    private var primaryColorAnimator: ValueAnimator? = null
    private var foregroundColorAnimator: ValueAnimator? = null
    private var imageApplierJob: Job? = null
    private var colorApplierJob: Job? = null
    private val colorApplier = MutableStateFlow(defaultColor)
    private val colorEvaluator = ArgbEvaluatorCompat()
    private val urlColorMap = mutableMapOf<String, Int>()

    private val defaultColorSelector = { palette: Palette ->
        val lightMuted = palette.getLightMutedColor(defaultColor)
        val lightVibrant = palette.getLightVibrantColor(lightMuted)
        val vibrant = palette.getVibrantColor(lightVibrant)
        val muted = palette.getMutedColor(defaultColor)
        val dark = palette.getDarkMutedColor(Color.BLACK)
        muted
    }

    private val defaultColorModifier = { color: Int -> color }

    init {
        if (!backgroundManager.isAttached) {
            backgroundManager.isAutoReleaseOnStop = false
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
        instantApplyForeground(true)
    }

    fun applyDefault() {
        applyColor(defaultColor)
    }

    fun applyImage(
        url: String,
        colorSelector: (Palette) -> Int? = defaultColorSelector,
        colorModifier: (Int) -> Int = defaultColorModifier
    ) {
        val color = urlColorMap[url]
        if (colorSelector == defaultColorSelector && color != null) {
            applyColor(color, colorModifier)
            return
        }

        imageApplierJob?.cancel()
        imageApplierJob = activity.lifecycleScope.launch {
            runCatching {
                val bitmap = withContext(Dispatchers.IO) {
                    activity.loadImageBitmap(url)
                }
                withContext(Dispatchers.Default) {
                    bitmap.asSoftware {
                        Palette.Builder(it).generate()
                    }
                }
            }.onSuccess { palette ->
                if (colorSelector == defaultColorSelector) {
                    urlColorMap[url] = colorSelector(palette) ?: defaultColorSelector(palette)
                }
                applyPalette(palette, colorSelector, colorModifier)
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    fun applyPalette(
        palette: Palette,
        colorSelector: (Palette) -> Int? = defaultColorSelector,
        colorModifier: (Int) -> Int = defaultColorModifier
    ) {
        applyColor(colorSelector(palette) ?: defaultColorSelector(palette), colorModifier)
    }

    fun applyColor(@ColorInt color: Int, colorModifier: (Int) -> Int = defaultColorModifier) {
        val finalColor = colorModifier.invoke(color)
        subscribeColorApplier()
        colorApplier.value = finalColor
    }

    private fun instantApplyColor(@ColorInt color: Int) {
        imageApplierJob?.cancel()
        primaryColorAnimator?.cancel()
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