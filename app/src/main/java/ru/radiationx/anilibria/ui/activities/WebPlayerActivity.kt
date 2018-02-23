package ru.radiationx.anilibria.ui.activities

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.webkit.WebSettings
import com.devbrackets.android.exomedia.listener.*
import com.devbrackets.android.exomedia.ui.widget.VideoControls
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_moon.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.repository.VitalRepository
import java.lang.Exception
import java.util.*
import android.webkit.WebView
import android.webkit.WebViewClient
import ru.radiationx.anilibria.utils.Utils
import java.util.regex.Pattern


class WebPlayerActivity : AppCompatActivity() {

    companion object {
        const val ARG_URL = "iframe_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_moon)
        supportActionBar?.hide()

        webView.settings.apply {
            setAppCacheEnabled(false)
            cacheMode = WebSettings.LOAD_NO_CACHE
            javaScriptEnabled = true
        }
        intent?.let {
            it.getStringExtra(ARG_URL)?.let {
                val extraHeaders = HashMap<String, String>()
                extraHeaders["Referer"] = "https://www.anilibria.tv/"
                Log.e("lalala", "load url $it")
                webView.loadUrl(it, extraHeaders)
                webView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        val matcher = Pattern.compile("https?:\\/\\/(?:vk\\.com\\/video_ext|streamguard\\.cc)").matcher(url)
                        return if (matcher.find()) {
                            false
                        } else {
                            Utils.externalLink(url.orEmpty())
                            true
                        }
                    }
                }
            } ?: finish()
        } ?: finish()
    }

}
