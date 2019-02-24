package ru.radiationx.anilibria.model.data.holders

import io.reactivex.Observable
import ru.radiationx.anilibria.entity.app.auth.SocialAuth

interface SocialAuthHolder {
    fun get(): List<SocialAuth>
    fun observe(): Observable<List<SocialAuth>>
    fun save(items: List<SocialAuth>)
    fun delete()
}