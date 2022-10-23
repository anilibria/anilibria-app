package ru.radiationx.shared_app.common

import android.content.Context
import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream
import com.nostra13.universalimageloader.core.download.BaseImageDownloader
import okhttp3.Request
import ru.radiationx.data.di.providers.ApiClientWrapper
import java.io.InputStream
import javax.inject.Inject

class OkHttpImageDownloader @Inject constructor(
    private val context: Context,
    private val clientWrapper: ApiClientWrapper
) : BaseImageDownloader(context) {

    override fun getStreamFromNetwork(imageUri: String, extra: Any?): InputStream {
        if (!imageUri.contains("static.anilibria.tv")) {
            return super.getStreamFromNetwork(imageUri, extra)
        }
        val request = Request.Builder().url(imageUri).build()
        val response = clientWrapper.get().newCall(request).execute()
        val responseBody = response.body
        val inputStream = responseBody!!.byteStream()
        val contentLength = responseBody.contentLength().toInt()
        return ContentLengthInputStream(inputStream, contentLength)
    }
}