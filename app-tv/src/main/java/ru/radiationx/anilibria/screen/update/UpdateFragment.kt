package ru.radiationx.anilibria.screen.update

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.leanback.app.ProgressBarManager
import androidx.transition.Fade
import androidx.transition.TransitionManager
import kotlinx.android.synthetic.main.fragment_update.*
import permissions.dispatcher.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.viewModel
import ru.radiationx.shared_app.screen.ScopedFragment
import toothpick.ktp.binding.module
import javax.inject.Inject

class UpdateFragment : ScopedFragment(R.layout.fragment_update) {

    private val progressBarManager by lazy { ProgressBarManager() }

    @Inject
    lateinit var backgroundManager: GradientBackgroundManager

    private val viewModel by viewModel<UpdateViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backgroundManager.clearGradient()
        progressBarManager.setRootView(updateRoot)

        subscribeTo(viewModel.updateData) {
            val string = StringBuilder().apply {
                appendParam("Версия", it.name.orEmpty())
                appendParam("Дата", it.date.orEmpty())
                appendln("<br>")
                appendSection("Важно", it.important)
                appendSection("Добавлено", it.added)
                appendSection("Исправлено", it.fixed)
                appendSection("Изменено", it.changed)
            }
            updateDescription.text = Html.fromHtml(string.toString())
        }

        subscribeTo(viewModel.downloadActionTitle) {
            updateButton.text = it
        }

        subscribeTo(viewModel.downloadProgressShowState) {
            TransitionManager.beginDelayedTransition(view as ViewGroup)
            progressBar.isVisible = it
            progressText.isVisible = it
        }

        subscribeTo(viewModel.downloadProgressData) {
            progressBar.isIndeterminate = it == 0
            progressBar.progress = it
            progressText.text = "$it%"
        }

        subscribeTo(viewModel.progressState) {
            if (it) {
                progressBarManager.show()
            } else {
                progressBarManager.hide()
                updateButton.requestFocus()
                TransitionManager.beginDelayedTransition(updateRoot, Fade())
            }
            updateContainer.isVisible = !it
        }

        updateButton.setOnClickListener { viewModel.onActionClick() }
    }

    private fun StringBuilder.appendParam(title: String, value: String) {
        append("<b>$title:</b> $value<br>")
    }

    private fun StringBuilder.appendSection(title: String, changes: List<String>) {
        if (changes.isEmpty()) {
            return
        }
        append("<b>$title</b><br>")
        changes.forEachIndexed { index, s ->
            append("— ").append(s)
            if (index + 1 < changes.size) {
                append("<br>")
            }
        }
        append("<br>")
    }

}