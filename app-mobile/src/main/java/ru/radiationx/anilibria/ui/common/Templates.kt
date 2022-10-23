package ru.radiationx.anilibria.ui.common

import android.content.Context
import biz.source_code.miniTemplator.MiniTemplator
import timber.log.Timber
import toothpick.InjectConstructor
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.Charset

@InjectConstructor
class Templates(
    private val context: Context
) {

    val staticPageTemplate by lazy { findTemplate("static_page") }

    val vkCommentsTemplate by lazy { findTemplate("vk_comments") }

    val videoPageTemplate by lazy { findTemplate("video_page") }

    private fun findTemplate(name: String): MiniTemplator {
        val charset = Charset.forName("utf-8")
        return try {
            context.assets.open("templates/$name.html").use {
                MiniTemplator.Builder().build(it, charset)
            }
        } catch (e: IOException) {
            Timber.e(e)
            ByteArrayInputStream("Template error!".toByteArray(charset)).use {
                MiniTemplator.Builder().build(it, charset)
            }
        }
    }
}