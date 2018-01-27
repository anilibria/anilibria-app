package ru.radiationx.anilibria.model.repository

import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.data.remote.api.ReleaseApi
import ru.radiationx.anilibria.model.data.remote.parsers.VitalParser
import ru.radiationx.anilibria.model.system.SchedulersProvider
import java.util.concurrent.TimeUnit

/**
 * Created by radiationx on 27.01.18.
 */
class VitalRepository(
        private val schedulers: SchedulersProvider,
        private val releaseApi: ReleaseApi
) {

    private val testData = """{
  "items": [
    {
      "id": 0,
      "type": "item",
      "name": "мелкий баннер 1",
      "contentType": "image",
      "contentText": null,
      "contentImage": "https://www.anilibria.tv/bitrix/templates/AniLibria%20KD%20Design/images/reklama/fastanime_small42018.gif",
      "contentLink": null,
      "rules": [
        "articleList",
        "releaseDetail",
        "releaseList"
      ],
      "events": []
    },
    {
      "id": 1,
      "type": "fullscreen",
      "name": "полноэкранная хуйня 1",
      "contentType": "web",
      "contentText": "<center><ins class=\"e5771359\" data-key=\"5552e7692053a0262bbbd7f26322a975\"></ins><script async defer src=\"//aj1433.online/001fd7b2.js\"></script></center>",
      "contentImage": "https://www.anilibria.tv/bitrix/templates/AniLibria%20KD%20Design/images/reklama/fastanime_small42018.gif",
      "contentLink": "https://www.anilibria.tv/bitrix/templates/AniLibria%20KD%20Design/images/reklama/fastanime_small42018.gif",
      "rules": [
        "videoPlayer"
      ],
      "events": [
        "exitVideo"
      ]
    },
    {
      "id": 2,
      "type": "item",
      "name": "элемент в списке 1",
      "contentType": "web",
      "contentText": "<center><ins class=\"e5771359\" data-key=\"5552e7692053a0262bbbd7f26322a975\"></ins><script async defer src=\"//aj1433.online/001fd7b2.js\"></script></center>",
      "contentImage": null,
      "contentLink": null,
      "rules": [
        "releaseList",
        "articleList"
      ],
      "events": []
    },
    {
      "id": 3,
      "type": "item",
      "name": "элемент в полной версии (релиз/статья) 1",
      "contentType": "web",
      "contentText": "<b>ce text, lalalal</b>",
      "contentImage": null,
      "contentLink": null,
      "rules": [
        "releaseDetail",
        "articleDetail"
      ],
      "events": []
    }
  ]
}"""

    private val currentDataRelay = BehaviorRelay.create<List<VitalItem>>()
    private var currentLoader: Single<List<VitalItem>>? = null
    private val currentData = mutableListOf<VitalItem>()

    fun loadVital(): List<VitalItem> {
        val parser = VitalParser(App.injections.apiUtils)
        val parsed = parser.vital(testData)
        Log.e("VITAL", "parsed: ${parsed.size}")
        parsed.forEach {
            Log.e("VITAL", "\t\t${it.id}, ${it.type}, ${it.contentType}, ${it.rules.size}, ${it.events.size}")
        }
        return parsed
    }

    fun load() {
        Log.e("VITAL", "CALL LOAD")
        if (currentLoader == null && !currentDataRelay.hasValue()) {
            currentLoader = Single.fromCallable { loadVital() }
                    .delay(1, TimeUnit.SECONDS)
                    .doOnSuccess {
                        Log.e("VITAL", "LOAD SUCCESS ${it.size}")
                        currentDataRelay.accept(it)
                    }

            currentLoader
                    ?.subscribeOn(schedulers.io())
                    ?.observeOn(schedulers.ui())
                    ?.doAfterTerminate {
                        Log.e("VITAL", "doAfterTerminate")
                        currentLoader = null
                    }
                    ?.subscribe()
        }
    }

    fun observeByType(type: VitalItem.VitalType): Observable<List<VitalItem>> {
        Log.e("VITAL", "CALL GET BY TYPE: $type")
        return currentDataRelay
                .doOnSubscribe { load() }
                .map { it.filter { it.type == type } }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
    }

    fun observeByRule(rule: VitalItem.Rule) = observeByRules(listOf(rule))

    fun observeByRules(rules: List<VitalItem.Rule>): Observable<List<VitalItem>> {
        return currentDataRelay
                .doOnSubscribe { load() }
                .map { it.filter { it.rules.any { rules.contains(it) } } }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
    }

    fun observeByEvent(rule: VitalItem.EVENT) = observeByEvents(listOf(rule))

    fun observeByEvents(rules: List<VitalItem.EVENT>): Observable<List<VitalItem>> {
        return currentDataRelay
                .doOnSubscribe { load() }
                .map { it.filter { it.events.any { rules.contains(it) } } }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
    }
}