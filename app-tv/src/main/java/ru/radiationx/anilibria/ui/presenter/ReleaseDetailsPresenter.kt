package ru.radiationx.anilibria.ui.presenter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.TextViewCompat
import androidx.leanback.widget.RowPresenter
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import kotlinx.android.synthetic.main.row_detail_release.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.LibriaDetails
import ru.radiationx.anilibria.common.LibriaDetailsRow
import ru.radiationx.anilibria.extension.getCompatDrawable

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


        return ViewHolder(view)


    }

    override fun onBindRowViewHolder(vh: ViewHolder, item: Any) {
        super.onBindRowViewHolder(vh, item)
        val row = item as LibriaDetailsRow


        vh.view?.apply {
            updateLayoutParams {
                height = context.resources.displayMetrics.heightPixels - 1 // Шобы следующая строка подгрузилась при открытии
            }

            val details = row.details ?: return
            /*TransitionManager.beginDelayedTransition(this as ViewGroup, ChangeBounds().apply {
            })*/
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
            rowReleaseHQMarker.isVisible = details.hasFullHd

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

            rowReleaseActionContinue.setOnClickListener { continueClickListener.invoke() }
            rowReleaseActionPlay.setOnClickListener { playClickListener.invoke() }
            rowReleaseActionPlayWeb.setOnClickListener { playWebClickListener.invoke() }
            rowReleaseActionFavorite.setOnClickListener { favoriteClickListener.invoke() }
            rowReleaseDescriptionCard.setOnClickListener { descriptionClickListener.invoke() }


            if (rowReleaseImageCard.tag != details.image) {
                ImageLoader.getInstance().displayImage(details.image, rowReleaseImageCard, object : SimpleImageLoadingListener() {

                    override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                        super.onLoadingComplete(imageUri, view, loadedImage)
                        view?.tag = details.image
                    }
                })
            }
        }
    }


}