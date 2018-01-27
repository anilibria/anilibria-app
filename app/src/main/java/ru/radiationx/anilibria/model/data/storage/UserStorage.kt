package ru.radiationx.anilibria.model.data.storage

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.other.ProfileItem
import ru.radiationx.anilibria.entity.common.AuthState
import ru.radiationx.anilibria.model.data.holders.UserHolder

/**
 * Created by radiationx on 11.01.18.
 */
class UserStorage(private val sharedPreferences: SharedPreferences) : UserHolder {

    private val userRelay = BehaviorRelay.createDefault(getSavedUser())

    override fun getUser(): ProfileItem = userRelay.value

    private fun getSavedUser(): ProfileItem {
        val user = ProfileItem()
        val userSource = sharedPreferences.getString("saved_user", null)
        if (userSource == null) {
            user.authState = AuthState.NO_AUTH
            saveUser(user)
        } else {
            val userJson = JSONObject(userSource)
            user.apply {
                authState = AuthState.valueOf(userJson.getString("authState"))
                id = userJson.getInt("id")
                nick = userJson.getString("nick")
                avatarUrl = userJson.getString("avatar")
            }
        }
        return user
    }

    override fun observeUser(): Observable<ProfileItem> = userRelay

    override fun saveUser(user: ProfileItem) {
        val userJson = JSONObject()
        userJson.put("authState", user.authState.toString())
        userJson.put("id", user.id)
        userJson.put("nick", user.nick)
        userJson.put("avatar", user.avatarUrl)
        sharedPreferences.edit().putString("saved_user", userJson.toString()).apply()
        userRelay.accept(user)
    }

    override fun delete() {
        val user = getUser()
        user.apply {
            authState = AuthState.AUTH_SKIPPED
            id = ProfileItem.NO_ID
            nick = ProfileItem.NO_VALUE
            avatarUrl = ProfileItem.NO_VALUE
        }
        saveUser(user)
    }

}