package ru.radiationx.anilibria.common

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.BackgroundManager
import androidx.palette.graphics.Palette
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.jakewharton.rxrelay2.BehaviorRelay
import com.nostra13.universalimageloader.core.ImageLoader
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getCompatColor
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

@InjectConstructor
class GradientBackgroundManager(
    private val activity: FragmentActivity
) {

    private val backgroundManager: BackgroundManager by lazy { BackgroundManager.getInstance(activity) }

    private val defaultColor = activity.getCompatColor(R.color.dark_colorAccent)

    private val colorDrawable = ColorDrawable(defaultColor)
    private val gradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.BL_TR,
        intArrayOf(
            Color.parseColor("#ee000000"),
            Color.parseColor("#55000000")
        )
    )
    private val layerDrawable = LayerDrawable(arrayOf(colorDrawable, gradientDrawable))

    private var primaryColorAnimator: ValueAnimator? = null
    private var imageApplierDisposable = Disposables.disposed()
    private var colorApplierDisposable = Disposables.disposed()
    private val colorApplier = BehaviorRelay.create<Int>()
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
            backgroundManager.attach(activity.window)
            backgroundManager.isAutoReleaseOnStop = false
        }
    }

    private fun subscribeColorApplier() {
        if (!colorApplierDisposable.isDisposed) {
            colorApplierDisposable.dispose()
        }
        colorApplierDisposable = colorApplier
            .debounce(250, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                instantApplyColor(it)
            }
    }

    fun clearGradient() {
        imageApplierDisposable.dispose()
        colorApplierDisposable.dispose()
        backgroundManager.clearDrawable()
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

        imageApplierDisposable.dispose()
        imageApplierDisposable = Single
            .fromCallable {
                ImageLoader.getInstance().loadImageSync(url)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .map {
                Palette.Builder(it).generate()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (colorSelector == defaultColorSelector) {
                    urlColorMap[url] = colorSelector(it) ?: defaultColorSelector(it)
                }
                applyPalette(it, colorSelector, colorModifier)
            }, {
                it.printStackTrace()
            })
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
        colorApplier.accept(finalColor)
    }

    private fun instantApplyColor(@ColorInt color: Int) {
        imageApplierDisposable.dispose()
        if (backgroundManager.drawable == null) {
            backgroundManager.drawable = layerDrawable
        }

        primaryColorAnimator?.cancel()
        primaryColorAnimator = ValueAnimator
            .ofObject(colorEvaluator, colorDrawable.color, color)
            .apply {
                duration = 500
                addUpdateListener {
                    colorDrawable.color = it.animatedValue as Int
                }
                start()
            }
    }


}