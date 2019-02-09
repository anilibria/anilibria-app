package ru.radiationx.anilibria.ui.fragments.auth

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.fragment_auth_social.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.terrakok.cicerone.Router
import java.util.regex.Pattern


/**
 * Created by radiationx on 31.12.17.
 */
class AuthSocialFragment : BaseFragment() {

    companion object {
        const val ARG_SOCIAL_URL = "ARG_SOCIAL_URL"
        const val RETURN_URL = 1337
    }

    private var socialUrl = ""
    private val resultUrlPattern = Pattern.compile("https?:\\/\\/(?:(?:www|api)?\\.)?anilibria\\.tv\\/[\\s\\S]*?\\?auth_service_id=(?:Patreon|VKontakte)(&code)?", Pattern.CASE_INSENSITIVE)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        arguments?.let {
            socialUrl = it.getString(ARG_SOCIAL_URL)
        }
    }

    override fun getLayoutResource(): Int = R.layout.fragment_auth_social

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appbarLayout.visibility = View.GONE

        setStatusBarVisibility(true)
        setStatusBarColor(view.context.getColorFromAttr(R.attr.cardBackground))

        webView.settings.apply {
            setAppCacheEnabled(false)
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        webView.loadUrl(socialUrl)


        webView.webViewClient = object : WebViewClient() {

            var loadingFinished = true
            var redirect = false

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.e("S_DEF_LOG", "OverrideUrlLoading: " + url)
                if (!loadingFinished) {
                    redirect = true
                }

                loadingFinished = false

                val matcher = resultUrlPattern.matcher(url)
                if (matcher.find()) {
                    val result = if (matcher.group(1) != null) url else ""
                    //todo
                    //(activity as RouterProvider).getRouter().exitWithResult(RETURN_URL, result)
                    getDependency(screenScope, Router::class.java).exit()
                    return true
                }
                //view.loadUrl(request.url.toString())
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadingFinished = false
                progressBar.visibility = View.VISIBLE

                Log.e("S_DEF_LOG", "ON onPageStarted")
                //SHOW LOADING IF IT ISNT ALREADY VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {

                Log.e("S_DEF_LOG", "ON onPageFinished")
                if (!redirect) {
                    loadingFinished = true
                }

                if (loadingFinished && !redirect) {
                    progressBar.visibility = View.GONE
                } else {
                    redirect = false
                }
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                progressBar.visibility = View.GONE
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onDestroyView() {
        webView.webViewClient = null
        webView.stopLoading()
        super.onDestroyView()
    }
}