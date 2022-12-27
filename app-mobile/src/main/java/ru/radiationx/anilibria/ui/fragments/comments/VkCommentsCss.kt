package ru.radiationx.anilibria.ui.fragments.comments

import android.content.Context
import toothpick.InjectConstructor

@InjectConstructor
class VkCommentsCss(
    private val context: Context
) {

    val light: String by lazy {
        readText("styles/vk_comments_fix_light.css")
    }

    val dark: String by lazy {
        readText("styles/vk_comments_fix_dark.css")
    }

    private fun readText(fileName: String): String {
        return context.assets.open(fileName).use { inputStream ->
            inputStream.bufferedReader().use { bufferedReader ->
                bufferedReader.readText()
            }
        }
    }
}