package ru.radiationx.anilibria.screen.update.warning

import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.shared_app.common.SystemUtils
import javax.inject.Inject

class UpdateWarningViewModel @Inject constructor(
    private val extra: UpdateWarningExtra,
    private val guidedRouter: GuidedRouter,
    private val systemUtils: SystemUtils,
) : LifecycleViewModel() {

    companion object {
        const val ACTION_OPEN_STORE = 1L
        const val ACTION_OPEN_STORE_SITE = 2L
        const val ACTION_OPEN_FILE = 3L
    }

    fun onLinkClick(actionId: Long) {
        when (actionId) {
            ACTION_OPEN_STORE -> {
                systemUtils.externalLink("https://www.rustore.ru/catalog/app/ru.radiationx.anilibria.app.tv")
            }

            ACTION_OPEN_STORE_SITE -> {
                systemUtils.externalLink("rustore://apps.rustore.ru/app/ru.radiationx.anilibria.app.tv")
            }

            ACTION_OPEN_FILE -> {
                systemUtils.externalLink(extra.link.url)
            }
        }
        guidedRouter.close()
    }
}