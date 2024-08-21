package ru.radiationx.anilibria.ui.activities.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.quill.inject
import ru.radiationx.shared_app.common.SystemUtils

/**
 * Created by radiationx on 23.02.18.
 */
class IntentActivity : BaseActivity() {

    companion object {
        fun newIntent(context: Context, url: String? = null) =
            Intent(context, IntentActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = url?.let { Uri.parse(it) }
            }
    }

    private val linkHandler by inject<ILinkHandler>()

    private val systemUtils by inject<SystemUtils>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.data?.also { intentUri ->
            val screen = linkHandler.findScreen(intentUri.toString())
            if (screen != null) {
                val intent = Screens.Main(intentUri.toString()).createIntent(this)
                startActivity(intent)
            } else {
                if (intentUri.scheme?.let { it.startsWith("https") || it.startsWith("http") } == true) {
                    systemUtils.externalLink(intentUri.toString())
                } else {
                    Toast.makeText(this, "Действие не поддерживается", Toast.LENGTH_SHORT).show()
                }
            }
        }
        finish()
    }
}