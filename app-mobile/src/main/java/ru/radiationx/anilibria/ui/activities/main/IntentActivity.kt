package ru.radiationx.anilibria.ui.activities.main

import android.content.Intent
import android.os.Bundle
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.shared_app.di.injectDependencies
import javax.inject.Inject

/**
 * Created by radiationx on 23.02.18.
 */
class IntentActivity : BaseActivity() {

    @Inject
    lateinit var linkHandler: ILinkHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
        intent?.data?.also { intentUri ->
            val screen = linkHandler.findScreen(intentUri.toString())
            if (screen != null) {
                startActivity(Intent(this@IntentActivity, MainActivity::class.java).apply {
                    data = intentUri
                })
            } else {
                Utils.externalLink(intentUri.toString())
            }
        }
        finish()
    }
}