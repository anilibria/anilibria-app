package ru.radiationx.anilibria.ui.presenter

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.ui.widget.PosterCardView
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.repository.AuthRepository
import ru.radiationx.data.repository.FavoriteRepository
import ru.radiationx.quill.Quill
import ru.radiationx.shared_app.imageloader.showImageUrl

class LibriaCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = PosterCardView(parent.context)
        return LibriaCardViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        item ?: return
        item as LibriaCard
        viewHolder as LibriaCardViewHolder
        viewHolder.bind(item)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        viewHolder as LibriaCardViewHolder
        viewHolder.unbind()
    }

    override fun onViewAttachedToWindow(viewHolder: ViewHolder) {
        super.onViewAttachedToWindow(viewHolder)
        (viewHolder as LibriaCardViewHolder).attach()
    }

    override fun onViewDetachedFromWindow(viewHolder: ViewHolder) {
        (viewHolder as LibriaCardViewHolder).detach()
        super.onViewDetachedFromWindow(viewHolder)
    }
}

class LibriaCardViewHolder(
    private val containerView: PosterCardView,
) : Presenter.ViewHolder(containerView) {

    private val grayscale = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
    private var boundCard: LibriaCard? = null
    private var badgeScope: CoroutineScope? = null
    private val favoriteRepository by lazy { Quill.getRootScope().get(FavoriteRepository::class) }
    private val authRepository by lazy { Quill.getRootScope().get(AuthRepository::class) }

    private val cardHeight by lazy {
        containerView.context.resources.getDimension(R.dimen.card_height).toInt()
    }
    private val cardReleaseWidth by lazy {
        containerView.context.resources.getDimension(R.dimen.card_release_width).toInt()
    }
    private val cardYoutubeWidth by lazy {
        containerView.context.resources.getDimension(R.dimen.card_youtube_width).toInt()
    }

    fun bind(item: LibriaCard) {
        detach()
        boundCard = item
        when (item.type) {
            is LibriaCard.Type.Release -> containerView.setMainImageDimensions(
                cardReleaseWidth,
                cardHeight
            )

            is LibriaCard.Type.Youtube -> containerView.setMainImageDimensions(
                cardYoutubeWidth,
                cardHeight
            )
        }
        containerView.mainImageView?.apply {
            colorFilter = if (item.isBlocked) grayscale else null
            showImageUrl(item.image)
        }
        updateBadge(item, item.isFavorite)
        if (containerView.isAttachedToWindow) attach()
    }

    fun unbind() {
        detach()
        boundCard = null
        containerView.mainImageView?.apply {
            showImageUrl(null)
            clearColorFilter()
        }
        containerView.showFavoriteBadge = false
        containerView.contentDescription = null
    }

    fun attach() {
        detach()
        val item = boundCard ?: return
        val release = item.type as? LibriaCard.Type.Release ?: return
        if (!item.showFavoriteBadge) return
        val scope = CoroutineScope(Dispatchers.Main.immediate + Job())
        badgeScope = scope
        scope.launch {
            combine(
                favoriteRepository.observeFavoriteChanges(release.releaseId),
                authRepository.observeAuthState(),
            ) { favoriteChange, authState ->
                (favoriteChange ?: item.isFavorite) && authState == AuthState.AUTH
            }.collect { updateBadge(item, it) }
        }
    }

    fun detach() {
        badgeScope?.cancel()
        badgeScope = null
    }

    private fun updateBadge(item: LibriaCard, isFavorite: Boolean) {
        val visible = item.type is LibriaCard.Type.Release && item.showFavoriteBadge && isFavorite
        containerView.showFavoriteBadge = visible
        containerView.contentDescription = listOfNotNull(
            item.title,
            item.description.takeIf { item.isBlocked },
            "В избранном".takeIf { visible },
        ).joinToString(". ")
    }
}
