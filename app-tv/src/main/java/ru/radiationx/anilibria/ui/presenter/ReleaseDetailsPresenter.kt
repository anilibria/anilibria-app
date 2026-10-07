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
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.DetailsState
import ru.radiationx.anilibria.common.LibriaDetails
import ru.radiationx.anilibria.common.LibriaDetailsRow
import ru.radiationx.anilibria.databinding.RowDetailReleaseBinding
import ru.radiationx.shared.ktx.android.getCompatColor
import ru.radiationx.shared.ktx.android.getCompatDrawable
import ru.radiationx.shared_app.imageloader.showImageUrl

class ReleaseDetailsPresenter(
    private val continueClickListener: () -> Unit,
    private val playClickListener: () -> Unit,
    private val favoriteClickListener: () -> Unit,
    private val descriptionClickListener: () -> Unit,
    private val otherClickListener: () -> Unit,
) : RowPresenter() {

    init {
        headerPresenter = null
    }

    override fun isUsingDefaultSelectEffect(): Boolean {
        return false
    }

    override fun createRowViewHolder(parent: ViewGroup): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.row_detail_release, parent, false)
        return LibriaReleaseViewHolder(
            view,
            continueClickListener,
            playClickListener,
            favoriteClickListener,
            descriptionClickListener,
            otherClickListener
        )
    }

    override fun onBindRowViewHolder(vh: ViewHolder, item: Any) {
        super.onBindRowViewHolder(vh, item)
        vh as LibriaReleaseViewHolder
        item as LibriaDetailsRow
        vh.bind(item)
    }

    override fun onUnbindRowViewHolder(vh: ViewHolder) {
        (vh as LibriaReleaseViewHolder).unbind()
        super.onUnbindRowViewHolder(vh)
    }

}

class LibriaReleaseViewHolder(
    itemView: View,
    private val continueClickListener: () -> Unit,
    private val playClickListener: () -> Unit,
    private val favoriteClickListener: () -> Unit,
    private val descriptionClickListener: () -> Unit,
    private val otherClickListener: () -> Unit,
) : RowPresenter.ViewHolder(itemView) {

    private val binding by lazy {
        RowDetailReleaseBinding.bind(view)
    }

    private var lastState: DetailsState? = null
    private var lastDetails: LibriaDetails? = null

    private val loadingIndicator = DelayedProgressIndicator(binding.rowReleaseLoadingProgress)
    private val updateIndicator = DelayedProgressIndicator(binding.rowReleaseUpdateProgress)

    init {
        binding.rowReleaseActionContinue.setOnClickListener { continueClickListener.invoke() }
        binding.rowReleaseActionPlay.setOnClickListener { playClickListener.invoke() }
        binding.rowReleaseActionOther.setOnClickListener { otherClickListener.invoke() }
        binding.rowReleaseActionFavorite.setOnClickListener { favoriteClickListener.invoke() }
        binding.rowReleaseDescriptionCard.setOnClickListener { descriptionClickListener.invoke() }
        binding.root.updateLayoutParams {
            // Keep lower rows outside the first layout while the header is loading.
            height = binding.root.resources.displayMetrics.heightPixels
        }
    }

    fun bind(item: LibriaDetailsRow) {
        val previousDetails = lastDetails
        val actionsWereVisible = binding.rowReleaseActions.isVisible
        item.state?.also { bindState(it) }
        item.details?.also { bindDetails(it) }

        val actionsReady = item.details?.actionsReady == true && item.state?.loadingProgress != true
        binding.rowReleaseActions.isInvisible = !actionsReady
        binding.rowReleaseArrow.isInvisible = !actionsReady
        binding.rowReleaseRoot.isFocusable = !actionsReady
        if (actionsReady && (!actionsWereVisible || previousDetails != item.details)) {
            listOf(
                binding.rowReleaseActionContinue,
                binding.rowReleaseActionPlay,
                binding.rowReleaseActionFavorite,
            ).firstOrNull { it.isVisible }?.requestFocus()
        }
    }

    private fun bindState(state: DetailsState) {
        if (lastState == state) {
            return
        }

        lastState = state
        binding.rowReleaseImageCard.isInvisible = state.loadingProgress

        loadingIndicator.setLoading(state.loadingProgress)
        updateIndicator.setLoading(state.updateProgress && !state.loadingProgress)
    }

    fun unbind() {
        loadingIndicator.setLoading(false)
        updateIndicator.setLoading(false)
        lastState = null
    }

    private fun bindDetails(details: LibriaDetails) {
        if (lastDetails == details) {
            return
        }
        lastDetails = details

        binding.rowReleaseTitleRu.text = details.titleRu
        binding.rowReleaseTitleEn.text = details.titleEn
        binding.rowReleaseExtra.text = details.extra
        binding.rowReleaseDescription.text = details.description
        binding.rowReleaseAnnounce.text = details.announce
        binding.rowReleaseAnnounce.isVisible = details.announce.isNotEmpty()
        binding.rowReleaseFavoriteCount.text = details.favoriteCount
        binding.rowReleaseFavoriteCount.isVisible = details.favoriteCount != "0"

        val favoriteDrawable = if (details.isFavorite) {
            binding.rowReleaseFavoriteCount.getCompatDrawable(R.drawable.ic_details_favorite_filled)
        } else {
            binding.rowReleaseFavoriteCount.getCompatDrawable(R.drawable.ic_details_favorite)
        }
        binding.rowReleaseFavoriteCount.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,
            null,
            favoriteDrawable,
            null
        )
        TextViewCompat.setCompoundDrawableTintList(
            binding.rowReleaseFavoriteCount,
            ColorStateList.valueOf(binding.rowReleaseFavoriteCount.getCompatColor(R.color.dark_textDefault))
        )
        binding.rowReleaseHQMarker.isVisible = details.hasFullHd

        binding.rowReleaseActionPlay.isVisible = details.hasEpisodes
        binding.rowReleaseActionContinue.isVisible = details.hasViewed
        binding.rowReleaseActionOther.isVisible = details.hasEpisodes || details.hasViewed
        binding.rowReleaseActionFavorite.text = if (details.isFavorite) {
            "Убрать из избранного"
        } else {
            "Добавить в избранное"
        }

        binding.rowReleaseImageCard.showImageUrl(details.image)
    }
}

/** Short loads never reveal a spinner; completed loads hide it immediately. */
private class DelayedProgressIndicator(private val view: View) : View.OnAttachStateChangeListener {

    private var loading = false
    private var showPending = false
    private val showIndicator = Runnable {
        showPending = false
        if (loading && view.isAttachedToWindow) {
            view.isInvisible = false
        }
    }

    init {
        view.isInvisible = true
        view.addOnAttachStateChangeListener(this)
    }

    fun setLoading(loading: Boolean) {
        this.loading = loading
        if (!loading) {
            cancelShow()
            view.isInvisible = true
        } else if (view.isAttachedToWindow && !view.isVisible && !showPending) {
            showPending = true
            view.postDelayed(showIndicator, 250L)
        }
    }

    override fun onViewAttachedToWindow(v: View) {
        setLoading(loading)
    }

    override fun onViewDetachedFromWindow(v: View) {
        cancelShow()
        view.isInvisible = true
    }

    private fun cancelShow() {
        view.removeCallbacks(showIndicator)
        showPending = false
    }
}
