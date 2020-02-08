package ru.radiationx.data.datasource.remote.parsers

import org.json.JSONObject
import ru.radiationx.shared.ktx.android.nullString
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.datasource.remote.address.ApiAddress
import ru.radiationx.data.datasource.remote.address.ApiProxy
import javax.inject.Inject

/**
 * Created by radiationx on 27.01.18.
 */
class ConfigurationParser @Inject constructor(
        private val apiUtils: IApiUtils
) {

    fun parse(responseJson: JSONObject): List<ApiAddress> {
        val result = mutableListOf<ApiAddress>()
        responseJson.getJSONArray("addresses")?.let {
            for (i in 0 until it.length()) {
                it.optJSONObject(i)?.let { addressJson ->
                    result.add(parseAddress(addressJson))
                }
            }
        }
        return result
    }

    private fun parseAddress(addressJson: JSONObject): ApiAddress {
        val ips = mutableListOf<String>()
        addressJson.getJSONArray("ips")?.let {
            for (i in 0 until it.length()) {
                it.getString(i)?.also {
                    ips.add(it)
                }
            }
        }

        val proxies = mutableListOf<ApiProxy>()
        addressJson.getJSONArray("proxies")?.let {
            for (i in 0 until it.length()) {
                it.optJSONObject(i)?.also { proxyJson ->
                    proxies.add(parseProxy(proxyJson))
                }
            }
        }
        return ApiAddress(
                addressJson.getString("tag"),
                addressJson.nullString("name"),
                addressJson.nullString("desc"),
                addressJson.getString("widgetsSite"),
                addressJson.getString("site"),
                addressJson.getString("baseImages"),
                addressJson.getString("base"),
                addressJson.getString("api"),
                ips,
                proxies
        )
    }

    private fun parseProxy(proxyJson: JSONObject): ApiProxy = ApiProxy(
            proxyJson.getString("tag"),
            proxyJson.nullString("name"),
            proxyJson.nullString("desc"),
            proxyJson.getString("ip"),
            proxyJson.getInt("port"),
            proxyJson.nullString("user"),
            proxyJson.nullString("password")
    )
}