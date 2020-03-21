package ru.radiationx.anilibria.ui.presenter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.leanback.widget.RowPresenter
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.row_detail_release.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.LibriaDetailsRow

class ReleaseDetailsPresenter : RowPresenter() {

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
        val details = row.details


        vh.view?.apply {
            updateLayoutParams {
                height = context.resources.displayMetrics.heightPixels - 1 // Шобы следующая строка подгрузилась при открытии
            }
            rowReleaseTitleRu.text = details.titleRu
            rowReleaseTitleEn.text = details.titleEn
            rowReleaseExtra.text = details.extra
            rowReleaseDescription.text = details.description
            rowReleaseAnnounce.text = details.announce
            rowReleaseDescriptionCard.setOnClickListener {  }

            ImageLoader.getInstance().displayImage(details.image, rowReleaseImageCard)
        }
    }


}