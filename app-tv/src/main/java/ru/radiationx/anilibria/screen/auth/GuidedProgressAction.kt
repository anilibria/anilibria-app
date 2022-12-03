package ru.radiationx.anilibria.screen.auth

import android.content.Context
import androidx.leanback.widget.GuidedAction

open class GuidedProgressAction : GuidedAction() {

    var showProgress = false

    abstract class BuilderBase<B : BuilderBase<B>>(context: Context) :
        GuidedAction.BuilderBase<B>(context) {

        private var showProgress = false

        fun showProgress(show: Boolean) {
            showProgress = show
        }

        protected fun applyValues(action: GuidedProgressAction) {
            super.applyValues(action)
            action.showProgress = showProgress
        }
    }

    class Builder(context: Context) : BuilderBase<Builder>(context) {

        fun build(): GuidedProgressAction = GuidedProgressAction()
            .apply {
                applyValues(this)
            }
    }
}