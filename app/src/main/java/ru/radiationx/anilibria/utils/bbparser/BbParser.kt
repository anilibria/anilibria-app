package ru.radiationx.anilibria.utils.bbparser

import ru.radiationx.anilibria.utils.bbparser.models.BbNode
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by radiationx on 18.01.18.
 */
class BbParser {
    private val bbPatternSrc = Pattern.compile("\\[(\\/)?(\\w+|\\*)(?:=([^\\]]+)|\\s([^\\]]+))?\\]|([\\s\\S]+?)(?=\\[|\$)")
    private val bbAttrsPatternSrc = Pattern.compile("(\\w+)=(?:[\"']([^\"]+)[\"']|([^\\]\\s]+))")

    fun parse(src: String): BbNode {
        var idCounter = 0

        val bbRoot = BbNode(idCounter)
        var lastElement: BbNode = bbRoot

        val openedElements = mutableListOf<BbNode>()

        var attrsMatcher: Matcher? = null
        val matcher = bbPatternSrc.matcher(src)

        idCounter++
        while (matcher.find()) {
            val tag = matcher.group(2)

            if (tag == null) {
                val text = matcher.group(5)/*.trim()*/
                //if (text.isNotBlank()) {
                val newElement = BbNode(idCounter, BbNode.NODE_TEXT)
                newElement.parent = lastElement
                newElement.text = text
                lastElement.childs.add(newElement)
                idCounter++
                //}
            } else {
                val rootAttr = matcher.group(3)
                val attrs = matcher.group(4)
                val isOpen = matcher.group(1) == null
                if (isOpen) {
                    val newElement = BbNode(idCounter, tag.toUpperCase())
                    newElement.parent = lastElement
                    if (rootAttr != null) {
                        newElement.attributes[newElement.tag] = rootAttr
                    }
                    if (attrs != null) {
                        attrsMatcher = if (attrsMatcher == null) {
                            bbAttrsPatternSrc.matcher(attrs)
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
                                newElement.attributes[key] = value
                            }
                        }
                    }

                    if (tag != "*") {
                        openedElements.add(newElement)
                    }
                    lastElement.childs.add(newElement)
                    idCounter++
                } else {
                    if (tag != "*") {
                        if (openedElements.isNotEmpty()) {
                            openedElements.removeAt(openedElements.lastIndex)
                        }
                    }

                }
                lastElement = if (openedElements.isEmpty()) bbRoot else openedElements.last()
            }

        }
        return bbRoot
    }
}