package ru.radiationx.data.app.vkcomments

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import ru.radiationx.data.app.DirectApi
import ru.radiationx.data.app.vkcomments.models.VkComments
import ru.radiationx.data.common.toBaseUrl
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class VkCommentsRepository @Inject constructor(
    private val api: DirectApi
) {

    suspend fun getComments(): VkComments {
        return withContext(Dispatchers.IO) {
            VkComments(
                baseUrl = "https://www.anilibria.tv/".toBaseUrl(),
                script = "<div id=\"vk_comments\"></div><script type=\"text/javascript\" src=\"https://vk.com/js/api/openapi.js?160\" async onload=\"VK.init({apiId: 5315207, onlyWidgets: true}); VK.Widgets.Comments('vk_comments', {limit: 8, attach: false});\" ></script>"
            )
        }
    }

    suspend fun checkVkBlocked(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                withTimeout(15_000) {
                    api.checkUrl("https://vk.com/")
                    false
                }
            } catch (ex: Throwable) {
                ex !is UnknownHostException
            }
        }
    }

}
