package ru.radiationx.anilibria.screen.auth

import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.leanback.widget.GuidedAction
import androidx.leanback.widget.GuidedActionsStylist
import ru.radiationx.anilibria.R

class GuidedProgressActionsStylist : GuidedActionsStylist() {

    companion object {
        const val PROGRESS_VIEW_TYPE = 2
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

    override fun onBindViewHolder(vh: ViewHolder, action: GuidedAction) {
        super.onBindViewHolder(vh, action)

        if (action is GuidedProgressAction) {
            vh.itemView.findViewById<ProgressBar>(R.id.guidedactions_item_progressBar).apply {
                isVisible = action.showProgress
            }
            vh.contentView?.isVisible = !action.showProgress
        }
    }
}