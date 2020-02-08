package ru.radiationx.anilibria.model.datasource.holders

import io.reactivex.Observable
import ru.radiationx.data.entity.app.auth.SocialAuth

interface SocialAuthHolder {
    fun get(): List<SocialAuth>
    fun observe(): Observable<List<SocialAuth>>
    fun save(items: List<SocialAuth>)
    fun delete()
}