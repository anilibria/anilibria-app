package ru.radiationx.anilibria.ui.adapters.other

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.ImageLoader
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_other_profile.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ProfileListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.fragments.other.ProfileItemState
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.radiationx.shared_app.di.DI

class ProfileItemDelegate(
    private val clickListener: (ProfileItemState) -> Unit,
    private val logoutClickListener: () -> Unit
) : AppAdapterDelegate<ProfileListItem, ListItem, ProfileItemDelegate.ViewHolder>(
    R.layout.item_other_profile,
    { it is ProfileListItem },
    { ViewHolder(it, clickListener, logoutClickListener) }
) {

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        (holder as ViewHolder).onDetach()
    }

    override fun bindData(item: ProfileListItem, holder: ViewHolder) = holder.bind(item.state)

    class ViewHolder(
        override val containerView: View,
        private val clickListener: (ProfileItemState) -> Unit,
        private val logoutClickListener: () -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private val dimensionsProvider = DI.get(DimensionsProvider::class.java)
        private var compositeDisposable = CompositeDisposable()

        init {
            compositeDisposable.add(dimensionsProvider.observe().subscribe {
                containerView.setPadding(
                    containerView.paddingLeft,
                    it.statusBar,
                    containerView.paddingRight,
                    containerView.paddingBottom
                )
            })
        }

        fun bind(state: ProfileItemState) {
            profileNick.text = state.title
            profileDesc.text = state.subtitle
            profileLogout.isVisible = state.hasAuth
            profileDesc.isVisible = state.subtitle != null
            ImageLoader.getInstance().displayImage(state.avatar, profileAvatar)

            containerView.setOnClickListener { clickListener(state) }
            profileLogout.setOnClickListener { logoutClickListener() }
        }

        fun onDetach() {
            compositeDisposable.clear()
        }
    }
}
