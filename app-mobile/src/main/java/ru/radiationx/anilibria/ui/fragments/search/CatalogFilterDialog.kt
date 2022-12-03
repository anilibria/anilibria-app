package ru.radiationx.anilibria.ui.fragments.search

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.DialogGenresBinding
import ru.radiationx.anilibria.extension.fillNavigationBarColor
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.presentation.search.CatalogFilterState
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.shared.ktx.android.visible


class CatalogFilterDialog(
    private val context: Context,
    private val listener: ClickListener
) {

    private val dialog: BottomSheetDialog = BottomSheetDialog(context)

    private val binding = DialogGenresBinding.inflate(LayoutInflater.from(context), null, false)

    private val filterComplete = binding.filterComplete

    private val sortingGroup = binding.sortingRadioGroup
    private val sortingPopular = binding.sortingPopular
    private val sortingNew = binding.sortingNew

    private val genresChipGroup = binding.genresChips
    private val genresChips = mutableListOf<Chip>()

    private val yearsChipGroup = binding.yearsChips
    private val yearsChips = mutableListOf<Chip>()

    private val seasonsChipGroup = binding.seasonsChips
    private val seasonsChips = mutableListOf<Chip>()

    private var updating = false
    private var currentState = CatalogFilterState()

    private var actionButton: View
    private var actionButtonText: TextView
    private var actionButtonCount: TextView

    private val genresChipListener =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (updating) return@OnCheckedChangeListener
            val item = currentState.genres.first { it.value.hashCode() == buttonView.id }

            updateForm {
                it.copy(genres = it.genres.modify(item, isChecked))
            }
        }

    private val yearsChipListener =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (updating) return@OnCheckedChangeListener
            val item = currentState.years.first { it.value.hashCode() == buttonView.id }

            updateForm {
                it.copy(years = it.years.modify(item, isChecked))
            }
        }

    private val seasonsChipListener =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (updating) return@OnCheckedChangeListener
            val item = currentState.seasons.first { it.value.hashCode() == buttonView.id }

            updateForm {
                it.copy(seasons = it.seasons.modify(item, isChecked))
            }
        }

    private val sortingListener = RadioGroup.OnCheckedChangeListener { _, _ ->
        if (updating) return@OnCheckedChangeListener
        val newSort = when {
            sortingPopular.isChecked -> SearchForm.Sort.RATING
            sortingNew.isChecked -> SearchForm.Sort.DATE
            else -> null
        }
        if (newSort != null) {
            updateForm { it.copy(sort = newSort) }
        }
    }

    private val completeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if (updating) return@OnCheckedChangeListener
        updateForm { it.copy(onlyCompleted = isChecked) }
    }

    init {
        dialog.setContentView(binding.root)
        val parentView = binding.root.parent as FrameLayout
        val coordinatorLayout = parentView.parent as CoordinatorLayout
        val bottomSheetView =
            coordinatorLayout.findViewById<ViewGroup>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheetView.apply {
            setPadding(
                paddingLeft,
                paddingTop,
                paddingRight,
                (resources.displayMetrics.density * 40).toInt()
            )
        }

        actionButton = LayoutInflater.from(context)
            .inflate(R.layout.picker_bottom_action_button, coordinatorLayout, false)
        actionButtonText = actionButton.findViewById(R.id.pickerActionText)
        actionButtonCount = actionButton.findViewById(R.id.pickerActionCounter)

        coordinatorLayout.addView(
            actionButton,
            (actionButton.layoutParams as CoordinatorLayout.LayoutParams).also {
                it.gravity = Gravity.BOTTOM
            })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actionButton.z = parentView.z
        }

        sortingGroup.setOnCheckedChangeListener(sortingListener)
        filterComplete.setOnCheckedChangeListener(completeListener)

        actionButton.setOnClickListener {
            dialog.dismiss()
            listener.onAccept(currentState)
        }

        dialog.setOnDismissListener {
            listener.onClose()
        }
        setState(currentState, true)
    }

    fun showDialog(state: CatalogFilterState) {
        setState(state)
        dialog.fillNavigationBarColor()
        dialog.show()
    }

    private fun setState(state: CatalogFilterState, force: Boolean = false) {
        updating = true
        val lastState = currentState
        val form = state.form
        val lastForm = lastState.form
        currentState = state

        if (force || state.genres != lastState.genres) {
            updateGenreViews()
        }
        if (force || state.years != lastState.years) {
            updateYearViews()
        }
        if (force || state.seasons != lastState.seasons) {
            updateSeasonViews()
        }

        if (force || form.genres != lastForm.genres) {
            updateCheckedGenres()
        }
        if (force || form.years != lastForm.years) {
            updateCheckedYears()
        }
        if (force || form.seasons != lastForm.seasons) {
            updateCheckedSeasons()
        }

        if (force || form.sort != lastForm.sort) {
            updateSorting()
        }

        if (force || form.onlyCompleted != lastForm.onlyCompleted) {
            updateComplete()
        }

        updateCounter()
        updating = false
    }

    private fun updateGenreViews() {
        genresChipGroup.removeAllViews()
        genresChips.clear()
        currentState.genres.forEach { genre ->
            val chip = Chip(genresChipGroup.context).also {
                it.id = genre.value.hashCode()
                it.text = genre.title
                it.isCheckable = true
                it.isClickable = true
                it.isChecked = currentState.form.genres.contains(genre)
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
        currentState.years.forEach { year ->
            val chip = Chip(yearsChipGroup.context).also {
                it.id = year.value.hashCode()
                it.text = year.title
                it.isCheckable = true
                it.isClickable = true
                it.isChecked = currentState.form.years.contains(year)
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
        currentState.seasons.forEach { season ->
            val chip = Chip(seasonsChipGroup.context).also {
                it.id = season.value.hashCode()
                it.text = season.title
                it.isCheckable = true
                it.isClickable = true
                it.isChecked = currentState.form.seasons.contains(season)
                it.setTextColor(it.context.getColorFromAttr(R.attr.textDefault))
                it.setChipBackgroundColorResource(R.color.bg_chip)
                it.setOnCheckedChangeListener(seasonsChipListener)
            }
            seasonsChipGroup.addView(chip)
            seasonsChips.add(chip)
        }
    }

    private fun updateCheckedGenres() {
        genresChips.forEach { chip ->
            chip.isChecked = currentState.form.genres.any { it.value.hashCode() == chip.id }
        }
    }

    private fun updateCheckedYears() {
        yearsChips.forEach { chip ->
            chip.isChecked = currentState.form.years.any { it.value.hashCode() == chip.id }
        }
    }

    private fun updateCheckedSeasons() {
        seasonsChips.forEach { chip ->
            chip.isChecked = currentState.form.seasons.any { it.value.hashCode() == chip.id }
        }
    }

    private fun updateCounter() {
        val allCount = currentState.form.let {
            it.genres.size + it.years.size + it.seasons.size
        }
        actionButtonCount.text = "$allCount"
        actionButtonCount.visible(allCount > 0)
    }

    private fun updateSorting() {
        sortingGroup.setOnCheckedChangeListener(null)
        when (currentState.form.sort) {
            SearchForm.Sort.RATING -> sortingPopular.isChecked = true
            SearchForm.Sort.DATE -> sortingNew.isChecked = true
        }
        sortingGroup.setOnCheckedChangeListener(sortingListener)
    }

    private fun updateComplete() {
        filterComplete.setOnCheckedChangeListener(null)
        filterComplete.isChecked = currentState.form.onlyCompleted
        filterComplete.setOnCheckedChangeListener(completeListener)
    }

    private fun updateState(block: (CatalogFilterState) -> CatalogFilterState) {
        setState(block.invoke(currentState))
    }

    private fun updateForm(block: (SearchForm) -> SearchForm) {
        updateState { state ->
            state.copy(form = block.invoke(state.form))
        }
    }

    private fun <T> Set<T>.modify(item: T, checked: Boolean): Set<T> {
        return if (checked) {
            plus(item)
        } else {
            minus(item)
        }
    }

    interface ClickListener {
        fun onAccept(state: CatalogFilterState)
        fun onClose()
    }
}
