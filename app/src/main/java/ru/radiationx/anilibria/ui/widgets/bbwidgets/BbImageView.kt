package ru.radiationx.anilibria.ui.widgets.bbwidgets

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewSwitcher
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import kotlinx.android.synthetic.main.widget_bb_imageview.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.widgets.AspectRatioImageView
import ru.radiationx.anilibria.utils.bbparser.models.BbOp

/**
 * Created by radiationx on 21.01.18.
 */
class BbImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : ViewSwitcher(context, attrs) {
    init {
        View.inflate(context, R.layout.widget_bb_imageview, this)
        //val dens = resources.displayMetrics.density
        /*setPadding(
                0,
                (8 * dens).toInt(),
                0,
                (8 * dens).toInt()
        )*/
        imageView.adjustViewBounds = true
    }

    fun setBbImage(bbOps: List<BbOp>) {
        val styles = mutableListOf<String>()
        var src = ""
        var width: Int = -1
        var height: Int = -1
        bbOps.forEach {
            val node = it.node
            when (it.op) {
                BbOp.OPEN -> {
                    if (node.tag != "IMG") {
                        styles.add(node.tag)
                    } else {
                        node.childs.forEach {
                            src += it.text
                        }
                        src = src.trim()

                        node.attributes.forEach {
                            when (it.key.toUpperCase()) {
                                "WIDTH" -> width = it.value.toInt()
                                "HEIGHT" -> height = it.value.toInt()
                            }
                        }
                    }
                }
            }
        }


        Log.e("SUKA", "BBIMAGEVIEW TRY SIZE $width $height, $src")
        if (width != -1 && height != -1) {
            imageView.setAspectRatio(height.toFloat() / width.toFloat())
            imageView.setEnabledAspectRation(true)
        } else {
            imageView.setEnabledAspectRation(false)
        }

        ImageLoader.getInstance().displayImage(src, imageView, object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                displayedChild = 1
            }

            override fun onLoadingStarted(imageUri: String?, view: View?) {
                displayedChild = 0
            }

            override fun onLoadingCancelled(imageUri: String?, view: View?) {
                displayedChild = 0
            }

            override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
                displayedChild = 0
            }
        })
    }
}