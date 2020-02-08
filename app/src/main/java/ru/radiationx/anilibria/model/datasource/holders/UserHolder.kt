package ru.radiationx.anilibria.model.datasource.holders

import io.reactivex.Observable
import ru.radiationx.data.entity.app.other.ProfileItem

/**
 * Created by radiationx on 11.01.18.
 */
interface UserHolder {
    fun getUser(): ProfileItem

    fun observeUser(): Observable<ProfileItem>

    fun saveUser(user: ProfileItem)

    fun delete()
}