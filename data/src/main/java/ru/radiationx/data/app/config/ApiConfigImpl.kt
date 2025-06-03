package ru.radiationx.data.app.config

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import ru.radiationx.data.app.config.models.ApiAddress
import ru.radiationx.data.app.config.models.ApiAddressId
import ru.radiationx.data.common.Url
import ru.radiationx.data.common.toAbsoluteUrl
import ru.radiationx.data.common.toBaseUrl
import javax.inject.Inject

class ApiConfigImpl @Inject constructor() : ApiConfig {

    private val defaultAddress = ApiAddress(
        id = ApiAddressId(id = "default"),
        name = "default",
        description = null,
        widget = "https://www.anilibria.tv/".toBaseUrl(),
        site = "https://anilibria.wtf/".toBaseUrl(),
        image = "https://api.anilibria.app/".toBaseUrl(),
        api = "https://api.anilibria.app/".toBaseUrl(),
        status = "https://api.anilibria.app/api/v1/teams/roles".toAbsoluteUrl()
    )

    private val activeAddressState = MutableStateFlow<ApiAddress?>(null)

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

    fun setReady(address: ApiAddress) {
        activeAddressState.value = address
    }

    fun setDefault() {
        activeAddressState.value = defaultAddress
    }

    private val active: ApiAddress
        get() = activeAddressState.value ?: defaultAddress

    override val id: ApiAddressId
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