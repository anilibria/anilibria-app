package ru.radiationx.anilibria.ui.fragments.search

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.material.chip.Chip
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.RadioGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_genres.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.data.entity.app.release.GenreItem
import ru.radiationx.data.entity.app.release.SeasonItem
import ru.radiationx.data.entity.app.release.YearItem
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.shared.ktx.android.visible


class GenresDialog(
        private val context: Context,
        private val listener: ClickListener
) {
    private val dialog: BottomSheetDialog = BottomSheetDialog(context)
    private var rootView: View = LayoutInflater.from(context).inflate(ru.radiationx.anilibria.R.layout.dialog_genres, null, false)

    private val filterComplete = rootView.filterComplete

    private val sortingGroup = rootView.sortingRadioGroup
    private val sortingPopular = rootView.sortingPopular
    private val sortingNew = rootView.sortingNew

    private val genresChipGroup = rootView.genresChips
    private val genresChips = mutableListOf<Chip>()

    private val yearsChipGroup = rootView.yearsChips
    private val yearsChips = mutableListOf<Chip>()

    private val seasonsChipGroup = rootView.seasonsChips
    private val seasonsChips = mutableListOf<Chip>()

    private val genreItems = mutableListOf<GenreItem>()
    private val yearItems = mutableListOf<YearItem>()
    private val seasonItems = mutableListOf<SeasonItem>()

    private val checkedGenres = mutableSetOf<String>()
    private val checkedYears = mutableSetOf<String>()
    private val checkedSeasons = mutableSetOf<String>()

    private var currentSorting = ""
    private var currentComplete = false

    private var actionButton: View
    private var actionButtonText: TextView
    private var actionButtonCount: TextView


    private val genresChipListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (isChecked) {
            checkedGenres.add(genreItems.first { it.value.hashCode() == buttonView.id }.value)
        } else {
            checkedGenres.remove(genreItems.first { it.value.hashCode() == buttonView.id }.value)
        }
        listener.onCheckedGenres(checkedGenres.toList())
    }

    private val yearsChipListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (isChecked) {
            checkedYears.add(yearItems.first { it.value.hashCode() == buttonView.id }.value)
        } else {
            checkedYears.remove(yearItems.first { it.value.hashCode() == buttonView.id }.value)
        }
        listener.onCheckedYears(checkedYears.toList())
    }

    private val seasonsChipListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (isChecked) {
            checkedSeasons.add(seasonItems.first { it.value.hashCode() == buttonView.id }.value)
        } else {
            checkedSeasons.remove(seasonItems.first { it.value.hashCode() == buttonView.id }.value)
        }
        listener.onCheckedSeasons(checkedSeasons.toList())
    }

    private val sortingListener = RadioGroup.OnCheckedChangeListener { _, _ ->
        currentSorting = when {
            sortingPopular.isChecked -> "2"
            sortingNew.isChecked -> "1"
            else -> ""
        }
        listener.onChangeSorting(currentSorting)
    }

    private val completeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        currentComplete = isChecked
        listener.onChangeComplete(currentComplete)
    }

    init {
        dialog.setContentView(rootView)
        val parentView = rootView.parent as FrameLayout
        val coordinatorLayout = parentView.parent as androidx.coordinatorlayout.widget.CoordinatorLayout
        val bottomSheetView = coordinatorLayout.findViewById<ViewGroup>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheetView.apply {
            setPadding(
                    paddingLeft,
                    paddingTop,
                    paddingRight,
                    (resources.displayMetrics.density * 40).toInt()
            )
        }

        actionButton = LayoutInflater.from(context).inflate(R.layout.picker_bottom_action_button, coordinatorLayout, false)
        actionButtonText = actionButton.findViewById(R.id.pickerActionText)
        actionButtonCount = actionButton.findViewById(R.id.pickerActionCounter)

        coordinatorLayout.addView(actionButton, (actionButton.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams).also {
            it.gravity = Gravity.BOTTOM
        })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actionButton.z = parentView.z
        }

        sortingGroup.setOnCheckedChangeListener(sortingListener)
        filterComplete.setOnCheckedChangeListener(completeListener)

        actionButton.setOnClickListener {
            dialog.dismiss()
            listener.onAccept()
        }

        dialog.setOnDismissListener {
            listener.onAccept()
        }
    }

    fun setItems(newItems: List<GenreItem>) {
        genreItems.clear()
        genreItems.addAll(newItems)

        updateGenreViews()
        updateChecked()
    }

    fun setYears(newItems: List<YearItem>) {
        yearItems.clear()
        yearItems.addAll(newItems)

        updateYearViews()
        updateChecked()
    }

    fun setSeasons(newItems: List<SeasonItem>) {
        seasonItems.clear()
        seasonItems.addAll(newItems)

        updateSeasonViews()
        updateChecked()
    }

    private fun updateGenreViews() {
        genresChipGroup.removeAllViews()
        genresChips.clear()
        genreItems.forEach { genre ->
            val chip = Chip(genresChipGroup.context).also {
                Log.e("lululu", "set id=${genre.value.hashCode()} to '${genre.value}'")
                it.id = genre.value.hashCode()
                it.text = genre.title
                it.isCheckable = true
                it.isClickable = true
                it.isChecked = checkedGenres.contains(genre.value)
                it.setTextColor(it.context.getColorFromAttr(R.attr.textDefault))
                it.setChipBackgroundColorResource(R.color.bg_chip)
                it.setOnCheckedChangeListener(genresChipListener)
            }
            genresChipGroup.addView(chip)
            genresChips.add(chip)
        }
    }

    private fun updateYearViews() {
        yearsChipGroup.removeAllViews()
        yearsChips.clear()
        yearItems.forEach { year ->
            val chip = Chip(yearsChipGroup.context).also {
                Log.e("lululu", "set id=${year.value.hashCode()} to '${year.value}'")
                it.id = year.value.hashCode()
                it.text = year.title
                it.isCheckable = true
                it.isClickable = true
                it.isChecked = checkedGenres.contains(year.value)
                it.setTextColor(it.context.getColorFromAttr(R.attr.textDefault))
                it.setChipBackgroundColorResource(R.color.bg_chip)
                it.setOnCheckedChangeListener(yearsChipListener)
            }
            yearsChipGroup.addView(chip)
            yearsChips.add(chip)
        }
    }

    private fun updateSeasonViews() {
        seasonsChipGroup.removeAllViews()
        seasonsChips.clear()
        seasonItems.forEach { season ->
            val chip = Chip(seasonsChipGroup.context).also {
                Log.e("lululu", "set id=${season.value.hashCode()} to '${season.value}'")
                it.id = season.value.hashCode()
                it.text = season.title
                it.isCheckable = true
                it.isClickable = true
                it.isChecked = checkedGenres.contains(season.value)
                it.setTextColor(it.context.getColorFromAttr(R.attr.textDefault))
                it.setChipBackgroundColorResource(R.color.bg_chip)
                it.setOnCheckedChangeListener(seasonsChipListener)
            }
            seasonsChipGroup.addView(chip)
            seasonsChips.add(chip)
        }
    }

    private fun updateChecked() {
        genresChips.forEach { chip ->
            chip.isChecked = checkedGenres.any { it.hashCode() == chip.id }
        }
        yearsChips.forEach { chip ->
            chip.isChecked = checkedYears.any { it.hashCode() == chip.id }
        }
        seasonsChips.forEach { chip ->
            chip.isChecked = checkedSeasons.any { it.hashCode() == chip.id }
        }
        val allCount = checkedGenres.size + checkedYears.size + checkedSeasons.size
        actionButtonCount.text = "$allCount"
        actionButtonCount.visible(allCount > 0)
    }

    fun setCheckedGenres(items: List<String>) {
        checkedGenres.clear()
        checkedGenres.addAll(items)
        updateChecked()
    }

    fun setCheckedYears(items: List<String>) {
        checkedYears.clear()
        checkedYears.addAll(items)
        updateChecked()
    }

    fun setCheckedSeasons(items: List<String>) {
        checkedSeasons.clear()
        checkedSeasons.addAll(items)
        updateChecked()
    }

    fun setSorting(sorting: String) {
        currentSorting = sorting
        sortingGroup.setOnCheckedChangeListener(null)
        when (currentSorting) {
            "2" -> sortingPopular.isChecked = true
            "1" -> sortingNew.isChecked = true
        }
        sortingGroup.setOnCheckedChangeListener(sortingListener)
    }

    fun setComplete(complete: Boolean) {
        currentComplete = complete
        filterComplete.setOnCheckedChangeListener(null)
        filterComplete.isChecked = currentComplete
        filterComplete.setOnCheckedChangeListener(completeListener)
    }

    fun showDialog() {
        updateGenreViews()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setWhiteNavigationBar(dialog)
        }
        dialog.show()
        //expandDialog()
    }

    private fun expandDialog() {
        getBehavior()?.also {
            it.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun getBehavior(): BottomSheetBehavior<View>? {
        val bottomSheetInternal = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
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
            navigationBarDrawable.setColor(context.getColorFromAttr(R.attr.colorSurface))

            val layers = arrayOf<Drawable>(dimDrawable, navigationBarDrawable)

            val windowBackground = LayerDrawable(layers)
            windowBackground.setLayerInsetTop(1, metrics.heightPixels)

            window.setBackgroundDrawable(windowBackground)
        }
    }

    interface ClickListener {
        fun onAccept()
        fun onCheckedGenres(items: List<String>)
        fun onCheckedYears(items: List<String>)
        fun onCheckedSeasons(items: List<String>)
        fun onChangeSorting(sorting: String)
        fun onChangeComplete(complete: Boolean)
    }
}
