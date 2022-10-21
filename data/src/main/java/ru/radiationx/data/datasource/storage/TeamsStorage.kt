package ru.radiationx.data.datasource.storage

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import com.squareup.moshi.Moshi
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.DataPreferences
import ru.radiationx.data.datasource.holders.TeamsHolder
import ru.radiationx.data.entity.app.team.TeamsResponse
import ru.radiationx.data.entity.common.DataWrapper
import toothpick.InjectConstructor

@InjectConstructor
class TeamsStorage(
    private val moshi: Moshi,
    @DataPreferences private val sharedPreferences: SharedPreferences
) : TeamsHolder {

    companion object {
        private const val KEY_DONATION = "teams3"
    }

    private val dataAdapter by lazy {
        moshi.adapter(TeamsResponse::class.java)
    }

    private val dataRelay =
        BehaviorRelay.createDefault<DataWrapper<TeamsResponse>>(DataWrapper(null))

    override fun observe(): Observable<TeamsResponse> = dataRelay
        .hide()
        .filter { it.data != null }
        .map { requireNotNull(it.data) }
        .doOnSubscribe {
            if (dataRelay.value?.data == null) {
                updateCurrentData()
            }
        }

    override fun get(): Single<TeamsResponse> = Single
        .fromCallable {
            if (dataRelay.value?.data == null) {
                updateCurrentData()
            }
            requireNotNull(dataRelay.value?.data)
        }

    override fun save(data: TeamsResponse): Completable = Completable
        .fromAction {
            saveToPrefs(data)
            updateCurrentData()
        }

    override fun delete(): Completable = Completable
        .fromAction {
            deleteFromPrefs()
            updateCurrentData()
        }

    private fun updateCurrentData() {
        val prefsData = try {
            getFromPrefs()
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
        dataRelay.accept(DataWrapper(prefsData))
    }

    private fun deleteFromPrefs() {
        sharedPreferences.edit().remove(KEY_DONATION).apply()
    }

    private fun saveToPrefs(data: TeamsResponse) {
        val json = dataAdapter.toJson(data)
        sharedPreferences.edit()
            .putString(KEY_DONATION, json)
            .apply()
    }

    private fun getFromPrefs(): TeamsResponse? = sharedPreferences
        .getString(KEY_DONATION, null)
        ?.let { dataAdapter.fromJson(it) }
}