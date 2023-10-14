package ru.radiationx.anilibria

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.moshi.Moshi
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.ui.activities.main.IntentActivity
import ru.radiationx.anilibria.ui.activities.main.MainActivity
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.fetchResponse
import ru.radiationx.data.datasource.storage.ApiConfigStorage
import ru.radiationx.data.entity.mapper.toDomain
import ru.radiationx.data.entity.response.config.ApiConfigResponse
import ru.radiationx.quill.get
import ru.radiationx.shared.ktx.android.asMutableFlag
import ru.radiationx.shared.ktx.android.getCompatColor
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber

class NotificationService : FirebaseMessagingService() {

    companion object {
        private const val CALL_CHANNEL_ID = "main"
        private const val CALL_CHANNEL_NAME = "Общие"


        private const val CUSTOM_TYPE_APP_UPDATE = "app_update"
        private const val CUSTOM_TYPE_CONFIG = "config"
    }

    private data class Data(
        val title: String,
        val body: String,
        val url: String? = null,
        val type: String? = null,
        val payload: String? = null
    )

    @OptIn(DelicateCoroutinesApi::class)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = message.notification
        val data = if (notification != null) {
            Data(
                notification.title.defaultTitle(),
                notification.body.defaultBody(),
                notification.link?.toString()
            )
        } else {
            Data(
                message.data["title"].defaultTitle(),
                message.data["body"].defaultBody(),
                message.data["link"],
                message.data["push_type"],
                message.data["push_data"]
            )
        }
        manager.notify(System.nanoTime().toInt(), getNotification(data))


        if (data.type == CUSTOM_TYPE_CONFIG) {
            GlobalScope.launch {
                coRunCatching {
                    val apiConfig = get<ApiConfig>()
                    val apiConfigStorage = get<ApiConfigStorage>()
                    val moshi = get<Moshi>()

                    apiConfig.updateNeedConfig(true)

                    val payload = data.payload.orEmpty()
                    val configResponse = payload.fetchResponse<ApiConfigResponse>(moshi)
                    apiConfigStorage.save(configResponse)
                    apiConfig.setConfig(configResponse.toDomain())
                }.onFailure {
                    Timber.e(it)
                }
            }
        }
    }

    private fun String?.defaultTitle() = this ?: "Заголовок уведомления"
    private fun String?.defaultBody() = this
        ?: "Тело уведомления. Похоже кто-то забыл указать указать правильные данные ¯\\_(ツ)_/¯"

    private fun getNotification(remote: Data): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CALL_CHANNEL_ID,
                CALL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
        }
        return NotificationCompat
            .Builder(this, CALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_push_notification)
            .setColor(application.getCompatColor(R.color.alib_red))
            .setContentTitle(remote.title)
            .setAutoCancel(true)
            .setContentText(remote.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(remote.body))
            .setContentIntent(getPendingIntent(getDefaultIntent(remote)))
            .setChannelId(CALL_CHANNEL_ID)
            .build()
    }

    private fun getDefaultIntent(remote: Data): Intent {
        return when (remote.type) {
            CUSTOM_TYPE_APP_UPDATE -> {
                Screens.AppUpdateScreen(true, AnalyticsConstants.notification_push_update)
                    .getActivityIntent(this)
            }
            CUSTOM_TYPE_CONFIG -> Intent(this, MainActivity::class.java)
            else -> Intent(this, IntentActivity::class.java).apply {
                remote.url?.also { data = Uri.parse(it) }
                action = Intent.ACTION_VIEW
            }
        }
    }

    private fun getPendingIntent(defaultIntent: Intent): PendingIntent = PendingIntent
        .getActivities(
            this,
            System.currentTimeMillis().toInt(),
            arrayOf(defaultIntent),
            PendingIntent.FLAG_UPDATE_CURRENT.asMutableFlag()
        )
}