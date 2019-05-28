package ru.radiationx.anilibria.ui.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import kotlinx.android.synthetic.main.activity_vital.*
import kotlinx.android.synthetic.main.item_vital_native.*
import kotlinx.android.synthetic.main.item_vital_web.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.extension.getMainStyleRes
import ru.radiationx.anilibria.model.data.holders.AppThemeHolder
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.utils.Utils
import javax.inject.Inject

/**
 * Created by radiationx on 27.01.18.
 */
class FullScreenActivity : BaseActivity() {
    companion object {
        const val VITAL_ITEM = "vitalik"
    }

    private lateinit var currentVital: VitalItem

    @Inject
    lateinit var appThemeHolder: AppThemeHolder

    @Inject
    lateinit var apiConfig: ApiConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
        setTheme(appThemeHolder.getTheme().getMainStyleRes())
        intent?.let {
            val vital = it.getSerializableExtra(VITAL_ITEM) as VitalItem?
            if (vital != null) {
                currentVital = vital
            } else {
                finish()
            }
        }
        /*initUiFlags()
        setUiFlags(true)*/
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_vital)

        if (currentVital.contentType == VitalItem.ContentType.WEB) {
            initWeb()
        } else if (currentVital.contentType == VitalItem.ContentType.IMAGE) {
            initImage()
        }

        closeButton.setOnClickListener {
            finish()
        }

    }

    private fun initWeb() {
        val view = View.inflate(this, R.layout.item_vital_web, null)
        fullScreenRoot.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        vitalWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webSwitcher.displayedChild = 0
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                webSwitcher.displayedChild = 1
            }

            @Suppress("OverridingDeprecatedMember")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Utils.externalLink(url.toString())
                return true
            }
        }
        vitalWebView.settings.apply {
            layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        }
        vitalWebView.setOnTouchListener { _, event -> event.action == MotionEvent.ACTION_MOVE }
        vitalWebView.easyLoadData(apiConfig.widgetsSiteUrl, currentVital.contentText)
    }

    private fun initImage() {
        val view = View.inflate(this, R.layout.item_vital_native, null)
        fullScreenRoot.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        imageSwitcher.setOnClickListener {
            currentVital.contentLink?.let { it1 -> Utils.externalLink(it1) }
        }
        ImageLoader.getInstance().displayImage(currentVital.contentImage, vitalImageView, object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                imageSwitcher.displayedChild = 0
            }

            override fun onLoadingStarted(imageUri: String?, view: View?) {
                imageSwitcher.displayedChild = 1
            }

            override fun onLoadingCancelled(imageUri: String?, view: View?) {
                imageSwitcher.displayedChild = 1
            }

            override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
                imageSwitcher.displayedChild = 1
            }
        }
        )
    }

    override fun onStop() {
        super.onStop()
        if (!isFinishing) {
            finish()
        }
    }
}