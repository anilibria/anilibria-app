package ru.radiationx.anilibria.ui.fragments.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.fragment_patreon.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import java.util.regex.Pattern

/**
 * Created by radiationx on 31.12.17.
 */
class PatreonFragment : BaseFragment() {

    val patreonBaseUrl = "https://www.patreon.com/oauth2/authorize?response_type=code&client_id=f1a0070b9517d52d8b548a5f1d64cbcc04dec2248bd76e976f63249a421ade38&redirect_uri=https%3A%2F%2Fwww.anilibria.tv%2F%3Fauth_service_id%3DPatreon&state=site_id%3Ds1%26backurl%3D%252F%253Fcheck_key%253D0a9a1086dd401c385b1bf5618d9d3458%2526logout_butt%253D%2525D0%252592%2525D1%25258B%2525D0%2525B9%2525D1%252582%2525D0%2525B8%26redirect_url%3D%252F"
    var patreonUrl = ""


    val pattern = "https?:\\/\\/(?:www\\.)?anilibria\\.tv\\/[\\s\\S]*?(\\?auth_service_id=(?:Patreon|VKontakte)?&code=)"
    override fun onBackPressed(): Boolean {
        return false
    }

    override fun getLayoutResource(): Int = R.layout.fragment_patreon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            patreonUrl = it.getString("url")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.e("SUKA", "OverrideUrlLoading: " + url)
                val matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(url)
                if (matcher.find()) {
                    (activity as RouterProvider).router.exitWithResult(1488, url)
                    return true
                }
                return false
            }
        }
        webView.loadUrl(patreonUrl)
    }
}