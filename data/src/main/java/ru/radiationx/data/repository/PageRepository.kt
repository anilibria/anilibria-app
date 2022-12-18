package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.api.PageApi
import ru.radiationx.data.entity.domain.page.PageLibria
import ru.radiationx.data.entity.domain.page.VkComments
import ru.radiationx.data.entity.mapper.toDomain
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class PageRepository @Inject constructor(
    @MainClient private val mainClient: IClient,
    private val pageApi: PageApi
) {

    private var currentComments: VkComments? = null

    suspend fun getPage(pagePath: String): PageLibria = withContext(Dispatchers.IO) {
        pageApi
            .getPage(pagePath)
    }

    suspend fun getComments(): VkComments {
        return withContext(Dispatchers.IO) {
            currentComments ?: pageApi.getComments().toDomain().also {
                currentComments = it
            }
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
