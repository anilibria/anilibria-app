package ru.radiationx.anilibria.ui.activities.main

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import ru.radiationx.anilibria.extension.nullString
import io.reactivex.Completable
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.anilibria.ui.activities.BaseActivity
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
        Log.e("lalala", "IntentActivity intent: $intent")
        intent?.data?.also { intentUri ->
            val screen = linkHandler.findScreen(intentUri.toString())
            Log.e("lalala", "screen: $screen, url=$intentUri")
            if (screen != null) {
                startActivity(Intent(this@IntentActivity, MainActivity::class.java).apply {
                    data = intentUri
                })
            }
        }
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.e("lalala", "IntentActivity onnewintent $intent")
    }
}