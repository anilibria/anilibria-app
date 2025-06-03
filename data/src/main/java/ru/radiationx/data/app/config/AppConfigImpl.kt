package ru.radiationx.data.app.config

import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.data.app.config.models.AppConfigAddress
import ru.radiationx.data.app.config.models.AppConfigAddressId
import ru.radiationx.data.common.Url
import ru.radiationx.data.common.toAbsoluteUrl
import ru.radiationx.data.common.toBaseUrl
import javax.inject.Inject

class AppConfigImpl @Inject constructor() : AppConfig {

    private val defaultAddress = AppConfigAddress(
        id = AppConfigAddressId(id = "default"),
        name = "default",
        description = null,
        widget = "https://www.anilibria.tv/".toBaseUrl(),
        site = "https://anilibria.wtf/".toBaseUrl(),
        image = "https://api.anilibria.app/".toBaseUrl(),
        api = "https://api.anilibria.app/".toBaseUrl(),
        status = "https://api.anilibria.app/api/v1/teams/roles".toAbsoluteUrl()
    )

    private val activeAddressState = MutableStateFlow<AppConfigAddress?>(null)

    private var networkHash: Int? = null

    fun getNetworkHash(): Int? {
        return networkHash
    }

    fun setNetworkHash(hash: Int) {
        networkHash = hash
    }

    fun needsUpdateAddress(hash: Int): Boolean {
        return hash != networkHash
    }

    fun setReady(address: AppConfigAddress) {
        activeAddressState.value = address
    }

    fun setDefault() {
        activeAddressState.value = defaultAddress
    }

    private val active: AppConfigAddress
        get() = activeAddressState.value ?: defaultAddress

    override val id: AppConfigAddressId
        get() = active.id

    val name: String?
        get() = active.name

    override val description: String?
        get() = active.description

    override val widget: Url.Base
        get() = active.widget

    override val site: Url.Base
        get() = active.site

    override val image: Url.Base
        get() = active.image

    override val api: Url.Base
        get() = active.api

    val status: Url.Absolute
        get() = active.status

}