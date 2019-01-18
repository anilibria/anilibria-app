package ru.radiationx.anilibria.ui.fragments.search

import android.content.Context
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
                it.isChecked = checkedItems.contains(genre.value)
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

    interface ClickListener {
        fun onHide()
        fun onCheckedItems(items: List<String>)
    }
}
