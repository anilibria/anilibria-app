package ru.radiationx.anilibria.ui.activities.updatechecker

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.first
import ru.mintrocket.lib.mintpermissions.MintPermissionsController
import ru.mintrocket.lib.mintpermissions.ext.isGranted
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.shared.ktx.android.getCompatColor
import ru.radiationx.shared.ktx.android.immutableFlag

object UpdateNotificationHelper {

    @SuppressLint("MissingPermission")
    suspend fun showUpdateData(
        context: Context,
        update: UpdateDataState,
        permissionsController: MintPermissionsController
    ) {
        if (!update.hasUpdate) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsController
                .observe(Manifest.permission.POST_NOTIFICATIONS)
                .first { it.isGranted() }
        }
        val channelId = "anilibria_channel_updates"
        val channelName = "Обновления"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val mBuilder = NotificationCompat.Builder(context, channelId)

        val mNotificationManager = NotificationManagerCompat.from(context)

        mBuilder.setSmallIcon(R.drawable.ic_notify)
        mBuilder.color = context.getCompatColor(R.color.alib_red)

        mBuilder.setContentTitle("Обновление AniLibria")
        mBuilder.setContentText("Новая версия: ${update.name}")
        mBuilder.setChannelId(channelId)


        val notifyIntent =
            Screens.AppUpdateScreen(false, AnalyticsConstants.notification_local_update)
                .createIntent(context)
        val notifyPendingIntent =
            PendingIntent.getActivity(context, 0, notifyIntent, immutableFlag())
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