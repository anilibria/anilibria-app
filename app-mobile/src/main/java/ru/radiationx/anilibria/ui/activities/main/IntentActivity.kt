package ru.radiationx.anilibria.ui.activities.main

import android.content.Intent
import android.os.Bundle
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.quill.quillInject
import ru.radiationx.shared_app.common.SystemUtils

/**
 * Created by radiationx on 23.02.18.
 */
class IntentActivity : BaseActivity() {

    private val linkHandler by quillInject<ILinkHandler>()

    private val systemUtils by quillInject<SystemUtils>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.data?.also { intentUri ->
            val screen = linkHandler.findScreen(intentUri.toString())
            if (screen != null) {
                startActivity(Intent(this@IntentActivity, MainActivity::class.java).apply {
                    data = intentUri
                })
            } else {
                systemUtils.externalLink(intentUri.toString())
            }
        }
        finish()
    }
}