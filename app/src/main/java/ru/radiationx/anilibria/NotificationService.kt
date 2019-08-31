package ru.radiationx.anilibria

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ru.radiationx.anilibria.extension.getCompatColor
import ru.radiationx.anilibria.ui.activities.main.IntentActivity
import ru.radiationx.anilibria.ui.activities.main.MainActivity

class NotificationService : FirebaseMessagingService() {

    companion object {
        private const val CALL_CHANNEL_ID = "main"
        private const val CALL_CHANNEL_NAME = "Общие"
    }

    private data class Data(
            val title: String,
            val body: String,
            val url: String?
    )

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = message.notification
        val data = if (notification != null) {
            Data(
                    notification.title.orEmpty(),
                    notification.body.orEmpty(),
                    notification.link?.toString()
            )
        } else {
            Data(
                    message.data["title"].orEmpty(),
                    message.data["body"].orEmpty(),
                    message.data["link"]
            )
        }
        manager.notify((System.currentTimeMillis() / 1000).toInt(), getNotification(data))
        /*Log.e("NotificationService", "new message ${notification.let {
            "${it.title}, ${it.body}, ${it.color}, ${it.icon}, ${it.sound}, ${it.clickAction}, ${it.link}, ${it.imageUrl}, ${it.tag}, ${it.channelId}"
        }}")*/


    }

    private fun getNotification(remote: Data): Notification {
        Log.e("NotificationService", "getNotification $remote")
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
                .setContentIntent(PendingIntent.getActivities(this, System.currentTimeMillis().toInt(), arrayOf(Intent(this, IntentActivity::class.java).apply {
                    remote.url?.also {
                        data = Uri.parse(it)
                    }
                    action = Intent.ACTION_VIEW
                }), PendingIntent.FLAG_UPDATE_CURRENT))
                .setChannelId(CALL_CHANNEL_ID)
                .build()
    }
}