package ru.radiationx.anilibria.ui.activities.updatechecker

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ActivityUpdaterBinding
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.features.UpdaterAnalytics
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getColorFromAttr
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter

/**
 * Created by radiationx on 24.07.17.
 */

class UpdateCheckerActivity : BaseActivity(R.layout.activity_updater) {

    companion object {
        private const val ARG_FORCE = "force"
        private const val ARG_ANALYTICS_FROM = "from"

        fun newIntent(context: Context, force: Boolean, analyticsFrom: String) =
            Intent(context, UpdateCheckerActivity::class.java).apply {
                putExtra(ARG_FORCE, force)
                putExtra(ARG_ANALYTICS_FROM, analyticsFrom)
                action = Intent.ACTION_VIEW
            }
    }

    private val binding by viewBinding<ActivityUpdaterBinding>()

    private val useTimeCounter by lazy {
        LifecycleTimeCounter(viewModel::submitUseTime)
    }

    private val viewModel by viewModel<CheckerViewModel> {
        CheckerExtra(forceLoad = getExtraNotNull(ARG_FORCE))
    }

    private val apiUtils by inject<IApiUtils>()

    private val updaterAnalytics by inject<UpdaterAnalytics>()

    private val sharedBuildConfig by inject<SharedBuildConfig>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(useTimeCounter)

        intent?.getStringExtra(ARG_ANALYTICS_FROM)?.also {
            updaterAnalytics.open(it)
        }
        viewModel.checkUpdate()

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow_back)

        binding.currentInfo.text =
            generateCurrentInfo(sharedBuildConfig.versionName, sharedBuildConfig.buildDate)

        viewModel.state.onEach { state ->
            state.data?.also { showUpdateData(it) }
            setRefreshing(state.loading)
        }.launchIn(lifecycleScope)
    }

    private fun showUpdateData(update: UpdateData) {
        val currentVersionCode = sharedBuildConfig.versionCode

        if (update.code > currentVersionCode) {
            binding.updateInfo.text = generateCurrentInfo(update.name, update.date)
            addSection("Важно", update.important)
            addSection("Добавлено", update.added)
            addSection("Исправлено", update.fixed)
            addSection("Изменено", update.changed)

            binding.updateInfo.isVisible = true
            binding.updateButton.isVisible = true
            binding.divider.isVisible = true
        } else {
            binding.updateInfo.text =
                "Обновлений нет, но вы можете загрузить текущую версию еще раз"
            binding.updateInfo.isVisible = true
            binding.updateContent.isGone = true
            binding.divider.isGone = true
        }
        binding.updateButton.isVisible = true
        binding.updateButton.setOnClickListener {
            viewModel.onDownloadClick()
            openDownloadDialog(update)
        }
    }

    private fun openDownloadDialog(update: UpdateData) {
        if (update.links.isEmpty()) {
            return
        }
        if (update.links.size == 1) {
            val link = update.links.last()
            viewModel.onSourceDownloadClick(link)
            return
        }
        val titles = update.links.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Источник")
            .setItems(titles) { _, which ->
                val link = update.links[which]
                viewModel.onSourceDownloadClick(link)
            }
            .show()
    }

    private fun setRefreshing(isRefreshing: Boolean) {
        binding.progressBar.isVisible = isRefreshing
        binding.updateInfo.isGone = isRefreshing
        binding.updateContent.isGone = isRefreshing
        binding.updateButton.isGone = isRefreshing
        binding.divider.isGone = isRefreshing
    }

    private fun addSection(title: String, array: List<String>) {
        if (array.isEmpty()) {
            return
        }
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(0, 0, 0, (resources.displayMetrics.density * 24).toInt())

        val sectionTitle = TextView(this)
        sectionTitle.text = title
        sectionTitle.setPadding(0, 0, 0, (resources.displayMetrics.density * 8).toInt())
        sectionTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        sectionTitle.setTextColor(getColorFromAttr(R.attr.textDefault))
        root.addView(sectionTitle)

        val stringBuilder = StringBuilder()

        array.forEachIndexed { index, s ->
            stringBuilder.append("— ").append(s)
            if (index + 1 < array.size) {
                stringBuilder.append("<br>")
            }
        }

        val sectionText = TextView(this)
        sectionText.text = apiUtils.toHtml(stringBuilder.toString())
        sectionText.setPadding((resources.displayMetrics.density * 8).toInt(), 0, 0, 0)
        sectionText.setTextColor(getColorFromAttr(R.attr.textDefault))
        root.addView(sectionText)

        binding.updateContent.addView(
            root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun generateCurrentInfo(name: String?, date: String?): String {
        return String.format("Версия: %s\nСборка от: %s", name, date)
    }
}
