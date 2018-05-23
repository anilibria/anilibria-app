package ru.radiationx.anilibria.ui.activities.main

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.mintrocket.gisdelivery.extension.nullString
import io.reactivex.Completable
import io.reactivex.Single
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.BuildConfig
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem

/**
 * Created by radiationx on 23.02.18.
 */
class IntentActivity : AppCompatActivity() {

    companion object {
        const val KEY_RESTORE = "restore_data"
        const val ACTION_RESTORE = "anilibria.app.RESTORE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("lalala", "IntentActivity intent: $intent")
        intent?.also {
            if (it.action == ACTION_RESTORE) {
                it.extras?.let {
                    val json = it.getString(KEY_RESTORE)
                    if (json != null) {
                        restore(json)
                    } else {
                        Toast.makeText(this@IntentActivity, "Данные бекапа повреждены", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } else {
                it.data?.let {
                    val screen = App.injections.linkHandler.findScreen(it.toString())
                    Log.e("lalala", "screen: $screen, url=${it.toString()}")
                    if (screen != null) {
                        startActivity(Intent(this@IntentActivity, MainActivity::class.java).apply {
                            data = it
                        })
                    }
                }

                finish()
            }
        }
    }

    private fun restore(jsonString: String) {
        val historyStorage = App.injections.historyStorage
        val episodesStorage = App.injections.episodesCheckerStorage
        val schedulers = App.injections.schedulers
        Completable
                .fromCallable {
                    val json = JSONObject(jsonString)
                    val jsonReleases = JSONArray(json.getString("releases"))
                    val jsonEpisodes = JSONArray(json.getString("episodes"))

                    val localReleases = mutableListOf<ReleaseItem>()
                    (0 until jsonReleases.length()).forEach {
                        jsonReleases.getJSONObject(it).let {
                            localReleases.add(ReleaseItem().apply {
                                id = it.getInt("id")
                                idName = it.nullString("idName")
                                title = it.nullString("title")
                                originalTitle = it.nullString("originalTitle")
                                torrentLink = it.nullString("torrentLink")
                                link = it.nullString("link")
                                image = it.nullString("image")
                                episodesCount = it.nullString("episodesCount")
                                description = it.nullString("description")
                                val jsonSeasons = it.getJSONArray("seasons")
                                (0 until jsonSeasons.length()).mapTo(seasons) { jsonSeasons.getString(it) }
                                val jsonVoices = it.getJSONArray("voices")
                                (0 until jsonVoices.length()).mapTo(voices) { jsonVoices.getString(it) }
                                val jsonGenres = it.getJSONArray("genres")
                                (0 until jsonGenres.length()).mapTo(genres) { jsonGenres.getString(it) }
                                val jsonTypes = it.getJSONArray("types")
                                (0 until jsonTypes.length()).mapTo(types) { jsonTypes.getString(it) }
                            })
                        }
                    }

                    val localEpisodes = mutableListOf<ReleaseFull.Episode>()
                    (0 until jsonEpisodes.length()).forEach {
                        jsonEpisodes.getJSONObject(it).let {
                            localEpisodes.add(ReleaseFull.Episode().apply {
                                releaseId = it.getInt("releaseId")
                                id = it.getInt("id")
                                seek = it.optLong("seek", 0L)
                                isViewed = it.optBoolean("isViewed", false)
                                lastAccess = it.optLong("lastAccess", 0L)
                            })
                        }
                    }

                    historyStorage.putAllRelease(localReleases)
                    episodesStorage.putAllEpisode(localEpisodes)
                }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .doOnSubscribe {
                    Toast.makeText(App.instance.applicationContext, "Восстанавливаю данные", Toast.LENGTH_SHORT).show()
                }
                .subscribe({
                    Toast.makeText(App.instance.applicationContext, "Бекап успешно восстановлен", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@IntentActivity, MainActivity::class.java))
                    finish()
                }, {
                    it.printStackTrace()
                    Toast.makeText(App.instance.applicationContext, "Ошибка при восстановлении бекапа: $it", Toast.LENGTH_SHORT).show()
                    finish()
                })

    }
}