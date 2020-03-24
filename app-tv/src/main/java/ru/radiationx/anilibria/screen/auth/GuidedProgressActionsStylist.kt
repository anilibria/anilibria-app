package ru.radiationx.anilibria.screen.auth

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.leanback.widget.GuidedAction
import androidx.leanback.widget.GuidedActionsStylist
import kotlinx.android.synthetic.main.wizard_progress_action_item.view.*
import ru.radiationx.anilibria.R

class GuidedProgressActionsStylist : GuidedActionsStylist() {

    companion object {
        const val PROGRESS_VIEW_TYPE = 2
    }

    init {
        setAsButtonActions()
    }

    override fun getItemViewType(action: GuidedAction): Int {
        if (action is GuidedProgressAction) {
            return PROGRESS_VIEW_TYPE
        }
        return super.getItemViewType(action)
    }

    override fun onProvideItemLayoutId(viewType: Int): Int {
        if (viewType == PROGRESS_VIEW_TYPE) {
            return R.layout.wizard_progress_action_item
        }
        return super.onProvideItemLayoutId(viewType)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(vh: ViewHolder, action: GuidedAction) {
        super.onBindViewHolder(vh, action)

        if (action is GuidedProgressAction) {
            vh.itemView.apply {
                guidedactions_item_progressBar.isVisible = action.showProgress
            }
            vh.contentView.isVisible = !action.showProgress
        }
    }
}