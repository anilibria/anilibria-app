package ru.radiationx.anilibria.ui.presenter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.TextViewCompat
import androidx.leanback.widget.RowPresenter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.row_detail_release.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.DetailsState
import ru.radiationx.anilibria.common.LibriaDetails
import ru.radiationx.anilibria.common.LibriaDetailsRow
import ru.radiationx.anilibria.extension.getCompatColor
import ru.radiationx.anilibria.extension.getCompatDrawable
import ru.radiationx.shared_app.imageloader.showImageUrl

class ReleaseDetailsPresenter(
    private val continueClickListener: () -> Unit,
    private val playClickListener: () -> Unit,
    private val playWebClickListener: () -> Unit,
    private val favoriteClickListener: () -> Unit,
    private val descriptionClickListener: () -> Unit
) : RowPresenter() {

    init {
        headerPresenter = null
    }

    override fun isUsingDefaultSelectEffect(): Boolean {
        return false
    }

    override fun createRowViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_detail_release, parent, false)
        return LibriaReleaseViewHolder(
            view,
            continueClickListener,
            playClickListener,
            playWebClickListener,
            favoriteClickListener,
            descriptionClickListener
        )
    }

    override fun onBindRowViewHolder(vh: ViewHolder, item: Any) {
        super.onBindRowViewHolder(vh, item)
        vh as LibriaReleaseViewHolder
        item as LibriaDetailsRow
        vh.bind(item)
    }

}

class LibriaReleaseViewHolder(
    override val containerView: View,
    private val continueClickListener: () -> Unit,
    private val playClickListener: () -> Unit,
    private val playWebClickListener: () -> Unit,
    private val favoriteClickListener: () -> Unit,
    private val descriptionClickListener: () -> Unit
) : RowPresenter.ViewHolder(containerView), LayoutContainer {

    private var lastState: DetailsState? = null
    private var lastDetails: LibriaDetails? = null

    init {
        rowReleaseActionContinue.setOnClickListener { continueClickListener.invoke() }
        rowReleaseActionPlay.setOnClickListener { playClickListener.invoke() }
        rowReleaseActionPlayWeb.setOnClickListener { playWebClickListener.invoke() }
        rowReleaseActionFavorite.setOnClickListener { favoriteClickListener.invoke() }
        rowReleaseDescriptionCard.setOnClickListener { descriptionClickListener.invoke() }
        containerView.updateLayoutParams {
            height = containerView.resources.displayMetrics.heightPixels - 1 // Шобы следующая строка подгрузилась при открытии
        }
    }

    fun bind(item: LibriaDetailsRow) {
        containerView.updateLayoutParams {
            height = containerView.resources.displayMetrics.heightPixels - 1 // Шобы следующая строка подгрузилась при открытии
        }
        item.state?.also { bindState(it) }
        item.details?.also { bindDetails(it) }
    }

    private fun bindState(state: DetailsState) {
        if (lastState == state) {
            return
        }

        lastState = state
        rowReleaseRoot.isFocusable = state.loadingProgress

        rowReleaseActions.isInvisible = state.loadingProgress
        rowReleaseImageCard.isInvisible = state.loadingProgress

        rowReleaseLoadingProgress.isVisible = state.loadingProgress
        rowReleaseUpdateProgress.isVisible = state.updateProgress && !state.loadingProgress
    }

    private fun bindDetails(details: LibriaDetails) {
        if (lastDetails == details) {
            return
        }
        lastDetails = details

        rowReleaseTitleRu.text = details.titleRu
        rowReleaseTitleEn.text = details.titleEn
        rowReleaseExtra.text = details.extra
        rowReleaseDescription.text = details.description
        rowReleaseAnnounce.text = details.announce
        rowReleaseFavoriteCount.text = details.favoriteCount
        rowReleaseFavoriteCount.isVisible = details.favoriteCount != "0"

        val favoriteDrawable = if (details.isFavorite) {
            rowReleaseFavoriteCount.getCompatDrawable(R.drawable.ic_details_favorite_filled)
        } else {
            rowReleaseFavoriteCount.getCompatDrawable(R.drawable.ic_details_favorite)
        }
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
            rowReleaseFavoriteCount,
            null,
            null,
            favoriteDrawable,
            null
        )
        TextViewCompat.setCompoundDrawableTintList(
            rowReleaseFavoriteCount,
            ColorStateList.valueOf(rowReleaseFavoriteCount.getCompatColor(R.color.dark_textDefault))
        )
        rowReleaseHQMarker.isVisible = details.hasFullHd

        rowReleaseActionPlay.isVisible = details.hasEpisodes
        rowReleaseActionContinue.isVisible = details.hasViewed
        rowReleaseActionPlayWeb.isVisible = details.hasWebPlayer
        rowReleaseActionFavorite.text = if (details.isFavorite) {
            "Убрать из избранного"
        } else {
            "Добавить в избранное"
        }

        if (details.hasViewed) {
            rowReleaseActionContinue.requestFocus()
        } else {
            rowReleaseActionPlay.requestFocus()
        }

        rowReleaseImageCard.showImageUrl(details.image)
    }
}