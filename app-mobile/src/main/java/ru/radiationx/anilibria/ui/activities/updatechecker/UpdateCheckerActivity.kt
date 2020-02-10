package ru.radiationx.anilibria.ui.activities.updatechecker

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_updater.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import ru.radiationx.anilibria.BuildConfig
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.extension.getCompatColor
import ru.radiationx.anilibria.presentation.checker.CheckerPresenter
import ru.radiationx.anilibria.presentation.checker.CheckerView
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.entity.app.updater.UpdateData
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.visible
import javax.inject.Inject

/**
 * Created by radiationx on 24.07.17.
 */

@RuntimePermissions
class UpdateCheckerActivity : BaseActivity(), CheckerView {

    companion object {
        const val ARG_FORCE = "force"
    }

    @Inject
    lateinit var apiUtils: IApiUtils

    @InjectPresenter
    lateinit var presenter: CheckerPresenter

    @ProvidePresenter
    fun provideCheckerPresenter() = getDependency(CheckerPresenter::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_updater)

        intent?.let {
            presenter.forceLoad = it.getBooleanExtra(ARG_FORCE, false)
        }
        presenter.checkUpdate()

        toolbar.setNavigationOnClickListener { finish() }
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow_back)

        currentInfo.text = generateCurrentInfo(BuildConfig.VERSION_NAME, BuildConfig.BUILD_DATE)
    }

    override fun showUpdateData(update: UpdateData) {
        val currentVersionCode = BuildConfig.VERSION_CODE

        if (update.code > currentVersionCode) {
            updateInfo.text = generateCurrentInfo(update.name, update.date)
            addSection("Важно", update.important)
            addSection("Добавлено", update.added)
            addSection("Исправлено", update.fixed)
            addSection("Изменено", update.changed)

            updateInfo.visible()
            updateButton.visible()
            divider.visible()
        } else {
            updateInfo.text = "Обновлений нет, но вы можете загрузить текущую версию еще раз"
            updateInfo.visible()
            updateContent.gone()
            divider.gone()
        }
        updateButton.visible()
        updateButton.setOnClickListener {
            openDownloadDialog(update)
        }
    }

    private fun openDownloadDialog(update: UpdateData) {
        if (update.links.isEmpty()) {
            return
        }
        if (update.links.size == 1) {
            decideDownload(update.links.last())
            return
        }
        val titles = update.links.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
                .setTitle("Источник")
                .setItems(titles) { _, which ->
                    //Utils.externalLink(update.links[titles[which]].orEmpty())
                    decideDownload(update.links[which])
                }
                .show()
    }

    private fun decideDownload(link: UpdateData.UpdateLink) {
        when (link.type) {
            "file" -> systemDownloadWithPermissionCheck(link.url)
            "site" -> Utils.externalLink(link.url)
            else -> Utils.externalLink(link.url)
        }
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun systemDownload(url: String) {
        Utils.systemDownloader(this, url)
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun setRefreshing(isRefreshing: Boolean) {
        progressBar.visible(isRefreshing)
        updateInfo.gone(isRefreshing)
        updateContent.gone(isRefreshing)
        updateButton.gone(isRefreshing)
        divider.gone(isRefreshing)
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
        sectionTitle.setTextColor(getCompatColor(R.color.light_textDefault))
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
        sectionText.setTextColor(getCompatColor(R.color.light_textDefault))
        root.addView(sectionText)

        updateContent.addView(root, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    private fun generateCurrentInfo(name: String?, date: String?): String {
        return String.format("Версия: %s\nСборка от: %s", name, date)
    }
}
