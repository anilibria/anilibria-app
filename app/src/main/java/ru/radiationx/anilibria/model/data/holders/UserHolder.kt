package ru.radiationx.anilibria.model.data.holders

import io.reactivex.Observable
import ru.radiationx.anilibria.entity.app.other.ProfileItem

/**
 * Created by radiationx on 11.01.18.
 */
interface UserHolder {
    fun getUser(): ProfileItem

    fun observeUser(): Observable<ProfileItem>

    fun saveUser(user: ProfileItem)

    fun delete()
}