package ru.radiationx.anilibria.utils

import android.app.PendingIntent
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.shared.ktx.android.asSoftware
import ru.radiationx.shared.ktx.android.centerCrop
import ru.radiationx.shared.ktx.android.createAvatar
import ru.radiationx.shared.ktx.android.immutableFlag
import ru.radiationx.shared.ktx.android.use
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.imageloader.loadImageBitmap
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.min

class ShortcutHelper @Inject constructor(
    private val context: Context,
) {

    @OptIn(DelicateCoroutinesApi::class)
    fun addShortcut(data: Release) {
        GlobalScope.launch {
            coRunCatching {
                val loadedImage = withContext(Dispatchers.IO) {
                    context.loadImageBitmap(data.poster)
                }?: return@coRunCatching
                val minSize = min(loadedImage.width, loadedImage.height)
                val desiredSize = Resources.getSystem().displayMetrics.density * 48
                val scaleFactor = minSize / desiredSize
                val bmp = withContext(Dispatchers.Default) {
                    loadedImage.asSoftware {
                        it.centerCrop(minSize, minSize, scaleFactor).use { cropper ->
                            cropper.createAvatar(isCircle = true)
                        }
                    }
                }
                addShortcut(data, bmp)
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun addShortcut(data: Release, bitmap: Bitmap) = addShortcut(
        context,
        data.code.code,
        (data.title ?: data.titleEng).toString(),
        data.names.joinToString(" / ") { it },
        data.link.orEmpty(),
        bitmap
    )

    private fun addShortcut(
        context: Context,
        id: String,
        shortLabel: String,
        longLabel: String,
        url: String,
        bitmap: Bitmap,
    ) {
        val intent = Screens.IntentHandler(url).createIntent(context)
        val shortcut = ShortcutInfoCompat.Builder(context, id)
            .setShortLabel(shortLabel)
            .setLongLabel(longLabel)
            .setIcon(IconCompat.createWithBitmap(bitmap))
            .setIntent(intent)
            .build()

        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val callbackIntent = ShortcutManagerCompat.createShortcutResultIntent(context, shortcut)

            val successCallback =
                PendingIntent.getBroadcast(context, 0, callbackIntent, immutableFlag())

            ShortcutManagerCompat.requestPinShortcut(
                context,
                shortcut,
                successCallback.intentSender
            )
        }
    }

}