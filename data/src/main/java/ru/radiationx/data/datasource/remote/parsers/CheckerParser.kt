package ru.radiationx.data.datasource.remote.parsers

import org.json.JSONObject
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.datasource.remote.address.ApiAddress
import ru.radiationx.data.datasource.remote.address.ApiProxy
import ru.radiationx.data.entity.app.updater.UpdateData
import ru.radiationx.shared.ktx.android.mapObjects
import ru.radiationx.shared.ktx.android.nullString
import ru.radiationx.shared.ktx.android.toStringsList
import javax.inject.Inject

/**
 * Created by radiationx on 27.01.18.
 */
class CheckerParser @Inject constructor(
    private val apiUtils: IApiUtils
) {

    fun parseAddresses(responseJson: JSONObject): List<ApiAddress> {
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

    fun parse(responseJson: JSONObject): UpdateData {
        val jsonUpdate = responseJson.getJSONObject("update")

        val links = jsonUpdate.getJSONArray("links")?.mapObjects { linkJson ->
            UpdateData.UpdateLink(
                name = linkJson.optString("name", "Unknown"),
                url = linkJson.optString("url").orEmpty(),
                type = linkJson.optString("type", "site")
            )
        }

        return UpdateData(
            code = jsonUpdate.optInt("version_code", 0),
            build = jsonUpdate.optInt("version_build", 0),
            name = jsonUpdate.optString("version_name"),
            date = jsonUpdate.optString("build_date"),
            links = links.orEmpty(),
            important = jsonUpdate.getJSONArray("important")?.toStringsList().orEmpty(),
            added = jsonUpdate.getJSONArray("added")?.toStringsList().orEmpty(),
            fixed = jsonUpdate.getJSONArray("fixed")?.toStringsList().orEmpty(),
            changed = jsonUpdate.getJSONArray("changed")?.toStringsList().orEmpty()
        )
    }
}