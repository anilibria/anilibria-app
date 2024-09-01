package ru.radiationx.anilibria.ui.common

import android.net.Uri
import com.github.terrakok.cicerone.Router
import ru.radiationx.anilibria.navigation.BaseFragmentScreen
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.entity.domain.types.ReleaseCode
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Created by radiationx on 03.02.18.
 */
class LinkRouter @Inject constructor(
    private val releaseAnalytics: ReleaseAnalytics,
) : ILinkHandler {

    private val releaseDetail by lazy {
        Pattern.compile("\\/release\\/([\\s\\S]*?)\\.html|tracker\\/\\?ELEMENT_CODE=([^&]+)")
    }

    private val historyImport by lazy {
        Pattern.compile("^content[\\s\\S]*?\\.json\$")
    }

    override fun handle(url: String, router: Router?, doNavigate: Boolean): Boolean {
        findScreen(url)?.also { screen ->
            if (doNavigate) {
                sendNavigateAnalytics(screen)
                router?.navigateTo(screen)
            }
            return true
        }
        return false
    }

    override fun findScreen(url: String): BaseFragmentScreen? {
        if (checkUnsupported(url)) {
            return null
        }
        releaseDetail.matcher(url).let {
            if (it.find()) {
                val code = it.group(1) ?: it.group(2)
                return Screens.ReleaseDetails(code = ReleaseCode(code))
            }
        }
        historyImport.matcher(url).let {
            if (it.find()) {
                return Screens.History(Uri.parse(url))
            }
        }
        return null
    }

    private fun sendNavigateAnalytics(screen: BaseFragmentScreen) {
        when (screen) {
            is Screens.ReleaseDetails -> {
                releaseAnalytics.open(AnalyticsConstants.link_router, null, screen.code?.code)
            }
        }
    }

    private fun checkUnsupported(url: String): Boolean {
        if (url.contains("communication/forum")) return true
        return false
    }

}