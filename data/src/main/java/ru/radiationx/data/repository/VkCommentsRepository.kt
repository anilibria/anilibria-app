package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.entity.domain.page.VkComments
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class VkCommentsRepository @Inject constructor(
    @MainClient private val mainClient: IClient,
) {

    suspend fun getComments(): VkComments {
        return withContext(Dispatchers.IO) {
            VkComments(
                baseUrl = "https://www.anilibria.tv/",
                script = "<div id=\"vk_comments\"></div><script type=\"text/javascript\" src=\"https://vk.com/js/api/openapi.js?160\" async onload=\"VK.init({apiId: 5315207, onlyWidgets: true}); VK.Widgets.Comments('vk_comments', {limit: 8, attach: false});\" ></script>"
            )
        }
    }

    suspend fun checkVkBlocked(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                withTimeout(15_000) {
                    mainClient
                        .get("https://vk.com/", emptyMap())
                        .let { false }
                }
            } catch (ex: Throwable) {
                ex !is UnknownHostException
            }
        }
    }

}
