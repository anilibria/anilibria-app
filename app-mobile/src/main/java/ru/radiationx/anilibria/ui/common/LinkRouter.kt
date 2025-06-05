package ru.radiationx.anilibria.ui.common

import android.net.Uri
import com.github.terrakok.cicerone.Router
import ru.radiationx.anilibria.navigation.BaseFragmentScreen
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.common.ReleaseCode
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
        releaseCodeLegacy(url)?.let { code ->
            return Screens.ReleaseLoader(code = ReleaseCode(code))
        }
        releaseCode(url)?.let { code ->
            return Screens.ReleaseLoader(code = ReleaseCode(code))
        }
        historyImport.matcher(url).let {
            if (it.find()) {
                return Screens.History(Uri.parse(url))
            }
        }
        return null
    }

    private fun releaseCodeLegacy(url: String): String? {
        return releaseDetail.matcher(url).let {
            if (it.find()) {
                it.group(1) ?: it.group(2)
            } else {
                null
            }
        }
    }

    private fun releaseCode(url: String): String? {
        val segments = Uri.parse(url).pathSegments
        if (segments.getOrNull(0) != "anime") return null
        if (segments.getOrNull(1) != "releases") return null
        if (segments.getOrNull(2) != "release") return null
        return segments.getOrNull(3)
    }

    private fun sendNavigateAnalytics(screen: BaseFragmentScreen) {
        when (screen) {
            is Screens.ReleaseDetails -> {
                releaseAnalytics.open(AnalyticsConstants.link_router, screen.id.id)
            }

            is Screens.ReleaseLoader -> {
                releaseAnalytics.open(AnalyticsConstants.link_router, screen.code.code)
            }
        }
    }

}