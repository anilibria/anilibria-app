package ru.radiationx.anilibria.ui.fragments.search

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.support.design.chip.Chip
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import kotlinx.android.synthetic.main.dialog_genres.view.*
import ru.radiationx.anilibria.entity.app.release.GenreItem
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.DisplayMetrics
import android.os.Build
import android.support.annotation.RequiresApi
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.getColorFromAttr


class GenresDialog(
        private val context: Context,
        private val listener: ClickListener
) {
    private val dialog: BottomSheetDialog = BottomSheetDialog(context)
    private val items = mutableListOf<GenreItem>()
    private val checkedItems = mutableSetOf<String>()
    private val rootView = LayoutInflater.from(context).inflate(ru.radiationx.anilibria.R.layout.dialog_genres, null, false)
    private val chipGroup = rootView.genresChips
    private val titleView = rootView.genresTitle
    private val currentChips = mutableListOf<Chip>()

    private val chipListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (isChecked) {
            checkedItems.add(items.first { it.value.hashCode() == buttonView.id }.value)
        } else {
            checkedItems.remove(items.first { it.value.hashCode() == buttonView.id }.value)
        }
        listener.onCheckedItems(checkedItems.toList())
    }

    init {
        dialog.setOnDismissListener {
            listener.onHide()
        }
    }

    fun setItems(newItems: List<GenreItem>) {
        items.clear()
        items.addAll(newItems)

        Log.e("lululu", "setItems ${items.size}")
        updateViews()
        updateChecked()
    }

    private fun updateViews() {
        chipGroup.removeAllViews()
        currentChips.clear()
        items.forEach { genre ->
            val chip = Chip(chipGroup.context).also {
                Log.e("lululu", "set id=${genre.value.hashCode()} to '${genre.value}'")
                it.id = genre.value.hashCode()
                it.text = genre.title
                it.isCheckable = true
                it.isClickable = true
                it.isChecked = checkedItems.contains(genre.value)
                it.setTextColor(it.context.getColorFromAttr(R.attr.textDefault))
                it.setChipBackgroundColorResource(R.color.bg_chip)
                it.setOnCheckedChangeListener(chipListener)
            }
            chipGroup.addView(chip)
            currentChips.add(chip)
        }
    }

    private fun updateChecked() {
        currentChips.forEach { chip ->
            chip.isChecked = checkedItems.any { it.hashCode() == chip.id }
        }
    }

    fun setChecked(items: List<String>) {
        checkedItems.clear()
        checkedItems.addAll(items)
        updateChecked()
    }

    fun showDialog() {
        rootView.parent?.let {
            (it as ViewGroup).removeView(rootView)
        }
        updateViews()
        dialog.setContentView(rootView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setWhiteNavigationBar(dialog)
        }
        dialog.show()
        expandDialog()
    }

    private fun expandDialog() {
        getBehavior()?.also {
            it.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun getBehavior(): BottomSheetBehavior<View>? {
        val bottomSheetInternal = dialog.findViewById<View>(android.support.design.R.id.design_bottom_sheet)
        return BottomSheetBehavior.from(bottomSheetInternal!!)
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    private fun setWhiteNavigationBar(dialog: Dialog) {
        val window = dialog.window
        if (window != null) {
            val metrics = DisplayMetrics()
            window.windowManager.defaultDisplay.getMetrics(metrics)

            val dimDrawable = GradientDrawable()

            val navigationBarDrawable = GradientDrawable()
            navigationBarDrawable.shape = GradientDrawable.RECTANGLE
            navigationBarDrawable.setColor(context.getColorFromAttr(R.attr.cardBackground))

            val layers = arrayOf<Drawable>(dimDrawable, navigationBarDrawable)

            val windowBackground = LayerDrawable(layers)
            windowBackground.setLayerInsetTop(1, metrics.heightPixels)

            window.setBackgroundDrawable(windowBackground)
        }
    }

    interface ClickListener {
        fun onHide()
        fun onCheckedItems(items: List<String>)
    }
}
