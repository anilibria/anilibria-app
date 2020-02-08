package ru.radiationx.anilibria.ui.widgets

import android.graphics.Bitmap
import androidx.annotation.CallSuper
import android.view.View
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener

open class UILImageListener : SimpleImageLoadingListener() {

    @CallSuper
    override fun onLoadingStarted(imageUri: String?, view: View?) {
        super.onLoadingStarted(imageUri, view)
    }

    @CallSuper
    override fun onLoadingCancelled(imageUri: String?, view: View?) {
        super.onLoadingCancelled(imageUri, view)
        onLoadingFinally(imageUri, view)
    }

    @CallSuper
    override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason) {
        super.onLoadingFailed(imageUri, view, failReason)
        onLoadingFinally(imageUri, view)
    }

    @CallSuper
    override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap) {
        super.onLoadingComplete(imageUri, view, loadedImage)
        onLoadingFinally(imageUri, view)
    }

    open fun onLoadingFinally(imageUrl: String?, view: View?) {}

}