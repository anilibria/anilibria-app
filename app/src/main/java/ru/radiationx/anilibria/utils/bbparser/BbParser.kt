package ru.radiationx.anilibria.utils.bbparser

import android.util.Log
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by radiationx on 18.01.18.
 */
class BbParser {
    private val bbPatternSrc = "\\[(\\/)?(\\w+)(?:=([^\\]]+)|\\s([^\\]]+))?\\]|([^\\[]+)"
    private val bbAttrsPatternSrc = "(\\w+)=(?:[\"']([^\"]+)[\"']|([^\\]\\s]+))"

    fun parse(src: String) {
        Log.e("BBPARSR", "SRC: $src")
        val bbRoot = BbCode()
        var lastElement: BbCode = bbRoot

        val openedElements = mutableListOf<BbCode>()

        var attrsMatcher: Matcher? = null
        val matcher = Pattern.compile(bbPatternSrc).matcher(src)
        while (matcher.find()) {
            val tag = matcher.group(2)

            if (tag == null) {
                val text = matcher.group(5)/*.trim()*/
                //if (text.isNotBlank()) {
                    val newElement = BbText()
                    newElement.text = text
                    lastElement.childs.add(newElement)
                //}
            } else {
                val rootAttr = matcher.group(3)
                val attrs = matcher.group(4)
                val isOpen = matcher.group(1) == null
                if (isOpen) {
                    val newElement = BbCode(tag.toUpperCase())

                    if (rootAttr != null) {
                        newElement.attributes.put(newElement.tag, rootAttr)
                    }
                    if (attrs != null) {
                        attrsMatcher = if (attrsMatcher == null) {
                            Pattern.compile(bbAttrsPatternSrc).matcher(attrs)
                        } else {
                            attrsMatcher.reset(attrs)
                        }
                        attrsMatcher?.let {
                            while (it.find()) {
                                val key = it.group(1)
                                var value = it.group(2)
                                if (value == null) {
                                    value = it.group(3)
                                }
                                newElement.attributes.put(key, value)
                            }
                        }
                    }

                    openedElements.add(newElement)
                    lastElement.childs.add(newElement)
                } else {
                    if (openedElements.isNotEmpty()) {
                        openedElements.removeAt(openedElements.lastIndex)
                    }
                }
            }
            lastElement = if (openedElements.isEmpty()) bbRoot else openedElements.last()
        }
        print(bbRoot)
    }

    var level = 0
    fun print(bbNode: BbNode) {
        val isCode = bbNode is BbCode
        val isText = bbNode is BbText
        var tabs = ""
        for (i in 0 until level) {
            tabs += "\t"
        }
        if (isCode) {
            var attrs = "{"
            (bbNode as BbCode).attributes.forEach {
                attrs += "${it.key}=${it.value} "
            }
            attrs += "}"
            Log.e("BBPARSR", "$tabs[${bbNode.tag}] attrs=$attrs")

        }
        if (bbNode is BbText) {
            Log.e("BBPARSR", "$tabs text = ${bbNode.text}")
        }
        if (bbNode is BbCode) {
            level++
            bbNode.childs.forEach {
                print(it)
            }
            level--
        }

        if (isCode) {
            Log.e("BBPARSR", "$tabs[/${bbNode.tag}]")
        }
    }

    open class BbNode {

        companion object {
            const val NODE_ROOT = "BBROOT"
            const val NODE_TEXT = "BBTEXT"
            const val ROOT_ATTRIBUTE = "ROOT_ATTR"
        }

        open lateinit var tag: String
    }

    class BbCode(override var tag: String = NODE_ROOT) : BbNode() {
        var attributes = mutableMapOf<String, String>()
        var childs = mutableListOf<BbNode>()
    }

    class BbText : BbNode() {
        override var tag: String = NODE_TEXT
        var text: String = ""
    }


}