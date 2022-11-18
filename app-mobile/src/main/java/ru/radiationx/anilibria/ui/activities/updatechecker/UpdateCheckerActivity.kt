package ru.radiationx.anilibria.ui.activities.updatechecker

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import by.kirich1409.viewbindingdelegate.viewBinding
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.BuildConfig
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ActivityUpdaterBinding
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.presentation.checker.CheckerPresenter
import ru.radiationx.anilibria.presentation.checker.CheckerView
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.data.analytics.features.UpdaterAnalytics
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.entity.domain.updater.UpdateData
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.visible
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter
import ru.radiationx.shared_app.di.getDependency
import ru.radiationx.shared_app.di.injectDependencies
import javax.inject.Inject

/**
 * Created by radiationx on 24.07.17.
 */

class UpdateCheckerActivity : BaseActivity(R.layout.activity_updater), CheckerView {

    companion object {
        private const val ARG_FORCE = "force"
        private const val ARG_ANALYTICS_FROM = "from"

        fun newIntent(context: Context, force: Boolean, analyticsFrom: String) =
            Intent(context, UpdateCheckerActivity::class.java).apply {
                putExtra(ARG_FORCE, true)
                putExtra(ARG_ANALYTICS_FROM, analyticsFrom)
                action = Intent.ACTION_VIEW
            }
    }

    private val binding by viewBinding<ActivityUpdaterBinding>()

    private val useTimeCounter by lazy {
        LifecycleTimeCounter(presenter::submitUseTime)
    }

    @Inject
    lateinit var apiUtils: IApiUtils

    @Inject
    lateinit var updaterAnalytics: UpdaterAnalytics

    @InjectPresenter
    lateinit var presenter: CheckerPresenter

    @ProvidePresenter
    fun provideCheckerPresenter() = getDependency(CheckerPresenter::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(useTimeCounter)

        intent?.let {
            presenter.forceLoad = it.getBooleanExtra(ARG_FORCE, false)
            it.getStringExtra(ARG_ANALYTICS_FROM)?.also {
                updaterAnalytics.open(it)
            }
        }
        presenter.checkUpdate()

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow_back)

        binding.currentInfo.text =
            generateCurrentInfo(BuildConfig.VERSION_NAME, BuildConfig.BUILD_DATE)
    }

    override fun showUpdateData(update: UpdateData) {
        val currentVersionCode = BuildConfig.VERSION_CODE

        if (update.code > currentVersionCode) {
            binding.updateInfo.text = generateCurrentInfo(update.name, update.date)
            addSection("Важно", update.important)
            addSection("Добавлено", update.added)
            addSection("Исправлено", update.fixed)
            addSection("Изменено", update.changed)

            binding.updateInfo.visible()
            binding.updateButton.visible()
            binding.divider.visible()
        } else {
            binding.updateInfo.text =
                "Обновлений нет, но вы можете загрузить текущую версию еще раз"
            binding.updateInfo.visible()
            binding.updateContent.gone()
            binding.divider.gone()
        }
        binding.updateButton.visible()
        binding.updateButton.setOnClickListener {
            presenter.onDownloadClick()
            openDownloadDialog(update)
        }
    }

    private fun openDownloadDialog(update: UpdateData) {
        if (update.links.isEmpty()) {
            return
        }
        if (update.links.size == 1) {
            val link = update.links.last()
            presenter.onSourceDownloadClick(link)
            return
        }
        val titles = update.links.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Источник")
            .setItems(titles) { _, which ->
                val link = update.links[which]
                presenter.onSourceDownloadClick(link)
            }
            .show()
    }

    override fun setRefreshing(isRefreshing: Boolean) {
        binding.progressBar.visible(isRefreshing)
        binding.updateInfo.gone(isRefreshing)
        binding.updateContent.gone(isRefreshing)
        binding.updateButton.gone(isRefreshing)
        binding.divider.gone(isRefreshing)
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
