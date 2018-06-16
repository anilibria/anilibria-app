package ru.radiationx.anilibria.ui.activities.updatechecker

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.BuildConfig
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.updater.UpdateData
import ru.radiationx.anilibria.model.repository.CheckerRepository

/**
 * Created by radiationx on 23.07.17.
 */

class SimpleUpdateChecker(private val checkerRepository: CheckerRepository) {

    fun checkUpdate() {
        checkerRepository
                .checkUpdate(BuildConfig.VERSION_CODE, true)
                .subscribe({
                    showUpdateData(it)
                }, {
                    it.printStackTrace()
                })
    }

    @SuppressLint("NewApi")
    private fun showUpdateData(update: UpdateData) {
        val currentVersionCode = BuildConfig.VERSION_CODE

        if (update.code > currentVersionCode) {
            val context: Context = App.instance
            val channelId = "anilibria_channel_updates"
            val channelName = "Обновления"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
                val manager = context.getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            }

            val mBuilder = NotificationCompat.Builder(context, channelId)

            val mNotificationManager = NotificationManagerCompat.from(context)

            mBuilder.setSmallIcon(R.drawable.ic_notify)
            mBuilder.color = ContextCompat.getColor(context, R.color.a_red)

            mBuilder.setContentTitle("Обновление AniLibria")
            mBuilder.setContentText("Новая версия: ${update.name}")
            mBuilder.setChannelId(channelId)


            val notifyIntent = Intent(context, UpdateCheckerActivity::class.java)
            notifyIntent.action = Intent.ACTION_VIEW
            val notifyPendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, 0)
            mBuilder.setContentIntent(notifyPendingIntent)

            mBuilder.setAutoCancel(true)

            mBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
            mBuilder.setCategory(NotificationCompat.CATEGORY_EVENT)

            var defaults = 0
            defaults = defaults or NotificationCompat.DEFAULT_SOUND
            defaults = defaults or NotificationCompat.DEFAULT_VIBRATE
            mBuilder.setDefaults(defaults)

            mNotificationManager.notify(update.code, mBuilder.build())
        }
    }
}
