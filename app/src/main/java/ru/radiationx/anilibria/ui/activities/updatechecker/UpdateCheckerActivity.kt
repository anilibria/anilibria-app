package ru.radiationx.anilibria.ui.activities.updatechecker

import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.activity_updater.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.BuildConfig
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.updater.UpdateData
import ru.radiationx.anilibria.presentation.checker.CheckerPresenter
import ru.radiationx.anilibria.presentation.checker.CheckerView
import ru.radiationx.anilibria.utils.Utils

/**
 * Created by radiationx on 24.07.17.
 */

class UpdateCheckerActivity : MvpAppCompatActivity(), CheckerView {

    @InjectPresenter
    lateinit var presenter: CheckerPresenter

    @ProvidePresenter
    fun provideCheckerPresenter() = CheckerPresenter(
            App.injections.checkerRepository
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_updater)

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

            updateInfo.visibility = View.VISIBLE
            updateContent.visibility = View.VISIBLE
            updateButton.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
            updateButton.setOnClickListener {
                openDownloadDialog(update)
            }
        } else {
            updateInfo.text = "Нет обновлений"
            updateInfo.visibility = View.VISIBLE
            updateContent.visibility = View.GONE
            updateButton.visibility = View.GONE
            divider.visibility = View.GONE
            updateButton.setOnClickListener(null)
        }
    }

    private fun openDownloadDialog(update: UpdateData) {
        if (update.links.isEmpty()) {
            return
        }
        if (update.links.size == 1) {
            Utils.externalLink(update.links.values.last())
            return
        }
        val titles = update.links.keys.toTypedArray()
        AlertDialog.Builder(this)
                .setTitle("Источник")
                .setItems(titles) { _, which ->
                    Utils.externalLink(update.links[titles[which]].orEmpty())
                }
                .show()
    }

    override fun setRefreshing(isRefreshing: Boolean) {
        if (isRefreshing) {
            progressBar.visibility = View.VISIBLE
            updateInfo.visibility = View.GONE
            updateContent.visibility = View.GONE
            updateButton.visibility = View.GONE
            divider.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            updateInfo.visibility = View.VISIBLE
            updateContent.visibility = View.VISIBLE
            updateButton.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
        }
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
        sectionTitle.setTextColor(ContextCompat.getColor(this, R.color.textDefault))
        root.addView(sectionTitle)

        val stringBuilder = StringBuilder()

        array.forEachIndexed { index, s ->
            stringBuilder.append("— ").append(s)
            if (index + 1 < array.size) {
                stringBuilder.append("<br>")
            }
        }

        val sectionText = TextView(this)
        sectionText.text = App.injections.apiUtils.toHtml(stringBuilder.toString())
        sectionText.setPadding((resources.displayMetrics.density * 8).toInt(), 0, 0, 0)
        sectionText.setTextColor(ContextCompat.getColor(this, R.color.textDefault))
        root.addView(sectionText)

        updateContent.addView(root, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    private fun generateCurrentInfo(name: String?, date: String?): String {
        return String.format("Версия: %s\nСборка от: %s", name, date)
    }

    /* override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults)
         App.get().onRequestPermissionsResult(requestCode, permissions, grantResults)
     }*/


}
